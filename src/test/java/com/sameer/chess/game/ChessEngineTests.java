package com.sameer.chess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.engine.AttackDetector;
import com.sameer.chess.engine.MoveExecutor;
import com.sameer.chess.engine.MoveValidator;
import com.sameer.chess.model.CastlingRights;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;
import com.sameer.chess.movegen.MoveGenerator;

class ChessEngineTests {
    @Test
    void initialPositionHasTwentyLegalMovesAndExpectedPerftCounts() {
        Game game = new Game();
        MoveGenerator generator = new MoveGenerator();
        MoveExecutor executor = new MoveExecutor();
        AttackDetector attackDetector = new AttackDetector();
        MoveValidator validator = MoveValidator.standard(generator, executor, attackDetector);

        assertEquals(20, game.getLegalMoves().size());
        assertEquals(20, perft(GameState.initial(), 1, validator, executor));
        assertEquals(400, perft(GameState.initial(), 2, validator, executor));
        assertEquals(8_902, perft(GameState.initial(), 3, validator, executor));
    }

    @Test
    void validMoveUpdatesBoardAndTurn() {
        Game game = new Game();
        MoveResult result = game.playMove(Move.parse("e2e4"));

        assertTrue(result.accepted(), result.message());
        assertEquals(Color.BLACK, game.getState().getActiveColor());
        assertEquals(Color.WHITE, game.getState().getBoard()
                .getPiece(Position.parse("e4")).color());
        assertEquals(Position.parse("e3"), game.getState().getEnPassantTarget());
    }

    @Test
    void rejectsWrongTurnAndDoesNotMutateState() {
        Game game = new Game();
        assertTrue(game.playMove(Move.parse("e2e4")).accepted());
        String before = boardKey(game.getState().getBoard());

        MoveResult result = game.playMove(Move.parse("b1c3"));

        assertFalse(result.accepted());
        assertEquals(before, boardKey(game.getState().getBoard()));
        assertEquals(1, game.getHistory().size());
    }

    @Test
    void rejectsIllegalPawnMoveWithoutChangingTheBoard() {
        Game game = new Game();
        String before = boardKey(game.getState().getBoard());

        assertFalse(game.playMove(Move.parse("e2e5")).accepted());

        assertEquals(before, boardKey(game.getState().getBoard()));
        assertTrue(game.getHistory().isEmpty());
    }

    @Test
    void rejectsBlockedSlidingMovesAndFriendlyCaptures() {
        Board board = Board.empty();
        put(board, "h1", Color.WHITE, PieceType.KING);
        put(board, "h8", Color.BLACK, PieceType.KING);
        put(board, "a1", Color.WHITE, PieceType.ROOK);
        put(board, "a3", Color.WHITE, PieceType.BISHOP);
        Game game = new Game(customState(board, Color.WHITE,
                new CastlingRights(false, false, false, false), 0));

        assertFalse(game.playMove(Move.parse("a1a4")).accepted());
        assertFalse(game.playMove(Move.parse("a1a3")).accepted());
        board.removePiece(Position.parse("a3"));
        put(board, "a3", Color.BLACK, PieceType.BISHOP);
        assertTrue(game.playMove(Move.parse("a1a3")).accepted());
        assertEquals(Color.WHITE, board.getPiece(Position.parse("a3")).color());
    }

    @Test
    void rejectsKingMovesIntoCheckAndPinnedPieceMoves() {
        Board kingBoard = Board.empty();
        put(kingBoard, "e1", Color.WHITE, PieceType.KING);
        put(kingBoard, "a8", Color.BLACK, PieceType.KING);
        put(kingBoard, "e8", Color.BLACK, PieceType.ROOK);
        Game kingGame = new Game(customState(kingBoard, Color.WHITE,
                new CastlingRights(false, false, false, false), 0));
        assertFalse(kingGame.playMove(Move.parse("e1e2")).accepted());

        Board pinBoard = Board.empty();
        put(pinBoard, "e1", Color.WHITE, PieceType.KING);
        put(pinBoard, "a8", Color.BLACK, PieceType.KING);
        put(pinBoard, "e8", Color.BLACK, PieceType.ROOK);
        put(pinBoard, "e2", Color.WHITE, PieceType.ROOK);
        Game pinGame = new Game(customState(pinBoard, Color.WHITE,
                new CastlingRights(false, false, false, false), 0));
        assertFalse(pinGame.playMove(Move.parse("e2d2")).accepted());
        assertTrue(pinGame.playMove(Move.parse("e2e3")).accepted());
    }

    @Test
    void allowsAResponseThatBlocksCheck() {
        Board board = Board.empty();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "a8", Color.BLACK, PieceType.KING);
        put(board, "e8", Color.BLACK, PieceType.ROOK);
        put(board, "a2", Color.WHITE, PieceType.ROOK);
        Game game = new Game(customState(board, Color.WHITE,
                new CastlingRights(false, false, false, false), 0));

        assertEquals(GameStatus.CHECK, game.getState().getStatus());
        assertFalse(game.playMove(Move.parse("a2a3")).accepted());
        assertTrue(game.playMove(Move.parse("a2e2")).accepted());
        assertEquals(GameStatus.ACTIVE, game.getState().getStatus());
    }

    @Test
    void pawnAttacksAreDiagonalAndNotForward() {
        Board board = Board.empty();
        put(board, "d5", Color.BLACK, PieceType.PAWN);
        AttackDetector detector = new AttackDetector();

        assertTrue(detector.isSquareAttacked(board,
                Position.parse("e4"), Color.BLACK));
        assertFalse(detector.isSquareAttacked(board,
                Position.parse("d4"), Color.BLACK));
    }

    @Test
    void detectsFoolsMateAndRejectsMovesAfterCheckmate() {
        Game game = new Game();
        play(game, "f2f3", "e7e5", "g2g4", "d8h4");

        assertEquals(GameStatus.CHECKMATE, game.getState().getStatus());
        assertTrue(game.isOver());
        assertFalse(game.playMove(Move.parse("e2e4")).accepted());
    }

    @Test
    void executesKingsideCastlingAndMovesTheRook() {
        Game game = new Game();
        play(game, "e2e4", "e7e5", "g1f3", "b8c6", "f1e2", "g8f6");
        assertTrue(game.playMove(Move.parse("e1g1")).accepted());

        Board board = game.getState().getBoard();
        assertEquals(PieceType.KING, board.getPiece(Position.parse("g1")).type());
        assertEquals(PieceType.ROOK, board.getPiece(Position.parse("f1")).type());
        assertTrue(board.isEmpty(Position.parse("h1")));
    }

    @Test
    void rejectsCastlingThroughAnAttackedSquare() {
        Board board = Board.empty();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "h1", Color.WHITE, PieceType.ROOK);
        put(board, "e8", Color.BLACK, PieceType.KING);
        put(board, "f8", Color.BLACK, PieceType.ROOK);
        Game game = new Game(customState(board, Color.WHITE,
                new CastlingRights(true, false, false, false), 0));

        assertFalse(game.playMove(Move.parse("e1g1")).accepted());
        assertEquals(PieceType.KING, board.getPiece(Position.parse("e1")).type());
    }

    @Test
    void executesQueensideCastling() {
        Board board = Board.empty();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "a1", Color.WHITE, PieceType.ROOK);
        put(board, "e8", Color.BLACK, PieceType.KING);
        Game game = new Game(customState(board, Color.WHITE,
                new CastlingRights(false, true, false, false), 0));

        MoveResult result = game.playMove(Move.parse("e1c1"));

        assertTrue(result.accepted(), result.message());
        assertEquals(PieceType.KING, board.getPiece(Position.parse("c1")).type());
        assertEquals(PieceType.ROOK, board.getPiece(Position.parse("d1")).type());
        assertNull(board.getPiece(Position.parse("a1")));
    }

    @Test
    void performsEnPassantAndRemovesTheCapturedPawn() {
        Game game = new Game();
        play(game, "e2e4", "a7a6", "e4e5", "d7d5");
        assertTrue(game.playMove(Move.parse("e5d6")).accepted());

        assertEquals(PieceType.PAWN, game.getState().getBoard()
                .getPiece(Position.parse("d6")).type());
        assertNull(game.getState().getBoard().getPiece(Position.parse("d5")));
    }

    @Test
    void rejectsEnPassantWhenItExposesTheKing() {
        Board board = Board.empty();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "a8", Color.BLACK, PieceType.KING);
        put(board, "e8", Color.BLACK, PieceType.ROOK);
        put(board, "e5", Color.WHITE, PieceType.PAWN);
        put(board, "d5", Color.BLACK, PieceType.PAWN);
        Game game = new Game(new GameState(board, Color.WHITE,
                new CastlingRights(false, false, false, false),
                Position.parse("d6"), 0, 1, GameStatus.ACTIVE));

        assertFalse(game.playMove(Move.parse("e5d6")).accepted());
        assertEquals(PieceType.PAWN, board.getPiece(Position.parse("e5")).type());
        assertEquals(PieceType.PAWN, board.getPiece(Position.parse("d5")).type());
    }

    @Test
    void promotesToEachSupportedPiece() {
        for (PieceType promotion : List.of(
                PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
            Board board = Board.empty();
            put(board, "h1", Color.WHITE, PieceType.KING);
            put(board, "h8", Color.BLACK, PieceType.KING);
            put(board, "a7", Color.WHITE, PieceType.PAWN);
            Game game = new Game(customState(board, Color.WHITE,
                    new CastlingRights(false, false, false, false), 0));

            Move move = new Move(Position.parse("a7"), Position.parse("a8"), promotion);
            MoveResult result = game.playMove(move);

            assertTrue(result.accepted(), result.message());
            assertEquals(promotion, board.getPiece(Position.parse("a8")).type());
        }
    }

    @Test
    void detectsThreefoldAsClaimableRatherThanAutomatic() {
        Game game = new Game();
        play(game,
                "b1c3", "b8c6", "c3b1", "c6b8",
                "b1c3", "b8c6", "c3b1", "c6b8");

        assertEquals(GameStatus.ACTIVE, game.getState().getStatus());
        assertTrue(game.canClaimThreefoldRepetitionDraw());
        assertTrue(game.claimThreefoldRepetitionDraw());
        assertEquals(GameStatus.DRAW_THREEFOLD_REPETITION, game.getState().getStatus());
    }

    @Test
    void detectsFivefoldRepetitionAutomatically() {
        Game game = new Game();
        for (int repetition = 0; repetition < 4; repetition++) {
            play(game, "b1c3", "b8c6", "c3b1", "c6b8");
        }

        assertEquals(GameStatus.DRAW_FIVEFOLD_REPETITION, game.getState().getStatus());
    }

    @Test
    void castlingRightsArePartOfTheRepetitionPosition() {
        GameState initial = GameState.initial();
        GameState withoutRights = new GameState(initial.getBoard().copy(), Color.WHITE,
                new CastlingRights(false, false, false, false), null,
                0, 1, GameStatus.ACTIVE);

        assertFalse(initial.positionKey(false).equals(withoutRights.positionKey(false)));
    }

    @Test
    void detectsStalemate() {
        Board board = Board.empty();
        put(board, "h8", Color.BLACK, PieceType.KING);
        put(board, "f7", Color.WHITE, PieceType.KING);
        put(board, "g6", Color.WHITE, PieceType.QUEEN);

        Game game = new Game(customState(board, Color.BLACK,
                new CastlingRights(false, false, false, false), 0));

        assertEquals(GameStatus.STALEMATE, game.getState().getStatus());
    }

    @Test
    void twoGamesHaveIndependentBoardAndHistory() {
        Game first = new Game();
        Game second = new Game();
        assertTrue(first.playMove(Move.parse("e2e4")).accepted());

        assertNotNull(first.getState().getBoard().getPiece(Position.parse("e4")));
        assertNull(second.getState().getBoard().getPiece(Position.parse("e4")));
        assertTrue(second.getHistory().isEmpty());
        assertEquals(20, second.getLegalMoves().size());
    }

    @Test
    void checkmateTakesPrecedenceOverSeventyFiveMoveDraw() {
        Board board = Board.empty();
        put(board, "h8", Color.BLACK, PieceType.KING);
        put(board, "f6", Color.WHITE, PieceType.KING);
        put(board, "g7", Color.WHITE, PieceType.QUEEN);
        Game game = new Game(customState(board, Color.BLACK,
                new CastlingRights(false, false, false, false), 150));

        assertEquals(GameStatus.CHECKMATE, game.getState().getStatus());
    }

    @Test
    void fiftyMoveDrawIsClaimable() {
        Board board = Board.empty();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "e8", Color.BLACK, PieceType.KING);
        put(board, "a1", Color.WHITE, PieceType.ROOK);
        Game game = new Game(customState(board, Color.WHITE,
                new CastlingRights(false, false, false, false), 100));

        assertTrue(game.canClaimFiftyMoveDraw());
        assertTrue(game.claimFiftyMoveDraw());
        assertEquals(GameStatus.DRAW_FIFTY_MOVE, game.getState().getStatus());
    }

    @Test
    void seventyFiveMoveDrawIsAutomatic() {
        Board board = Board.empty();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "e8", Color.BLACK, PieceType.KING);
        put(board, "a1", Color.WHITE, PieceType.ROOK);
        Game game = new Game(customState(board, Color.WHITE,
                new CastlingRights(false, false, false, false), 150));

        assertEquals(GameStatus.DRAW_SEVENTY_FIVE_MOVE, game.getState().getStatus());
        assertFalse(game.canClaimFiftyMoveDraw());
    }

    private static long perft(GameState state, int depth,
                              MoveValidator validator, MoveExecutor executor) {
        if (depth == 0) return 1;
        long nodes = 0;
        for (Move move : validator.generateLegalMoves(state)) {
            GameState child = state.copy();
            executor.applyUnchecked(child, move);
            nodes += perft(child, depth - 1, validator, executor);
        }
        return nodes;
    }

    private static String boardKey(Board board) {
        StringBuilder result = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                result.append(piece == null ? '.' : piece.displaySymbol());
            }
        }
        return result.toString();
    }

    private static void play(Game game, String... moves) {
        for (String move : moves) {
            MoveResult result = game.playMove(Move.parse(move));
            assertTrue(result.accepted(), move + ": " + result.message());
        }
    }

    private static void put(Board board, String square, Color color, PieceType type) {
        board.placePiece(Position.parse(square), new Piece(color, type));
    }

    private static GameState customState(Board board, Color active,
                                         CastlingRights castlingRights, int halfmoveClock) {
        return new GameState(board, active, castlingRights, null,
                halfmoveClock, 1, GameStatus.ACTIVE);
    }
}
