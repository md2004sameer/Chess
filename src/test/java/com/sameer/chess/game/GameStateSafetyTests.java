package com.sameer.chess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.CastlingRights;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

class GameStateSafetyTests {
    @Test
    void rejectedMoveLeavesBoardAndStateMetadataUnchanged() {
        Game game = new Game();
        GameState before = game.getState().copy();

        assertFalse(game.playMove(Move.parse("e2e5")).accepted());

        assertEquals(boardKey(before.getBoard()), boardKey(game.getState().getBoard()));
        assertEquals(before.getActiveColor(), game.getState().getActiveColor());
        assertEquals(before.getCastlingRights().key(), game.getState().getCastlingRights().key());
        assertEquals(before.getEnPassantTarget(), game.getState().getEnPassantTarget());
        assertEquals(before.getHalfmoveClock(), game.getState().getHalfmoveClock());
        assertEquals(before.getFullmoveNumber(), game.getState().getFullmoveNumber());
        assertEquals(before.getStatus(), game.getState().getStatus());
    }

    @Test
    void gameBoardAccessorReturnsAnIndependentCopy() {
        Game game = new Game();
        Board returned = game.board();

        assertNotSame(game.getState().getBoard(), returned);
        returned.setPiece(Position.parse("e2"), null);

        assertEquals(PieceType.PAWN, game.getState().getBoard()
                .getPiece(Position.parse("e2")).type());
    }

    @Test
    void returnedGameStateCannotMutateTheLiveGame() {
        Game game = new Game();
        GameState returned = game.getState();

        returned.getBoard().setPiece(Position.parse("e2"), null);
        returned.setActiveColor(Color.BLACK);
        returned.setStatus(GameStatus.CHECKMATE);
        returned.getCastlingRights().disableBoth(Color.WHITE);

        GameState current = game.getState();
        assertEquals(PieceType.PAWN, current.getBoard()
                .getPiece(Position.parse("e2")).type());
        assertEquals(Color.WHITE, current.getActiveColor());
        assertEquals(GameStatus.ACTIVE, current.getStatus());
        assertTrue(current.getCastlingRights().canCastle(Color.WHITE, true));
        assertTrue(game.playMove(Move.parse("e2e4")).accepted());
    }

    @Test
    void gameCopiesTheSuppliedStateAndItsMutableComponents() {
        GameState supplied = GameState.initial();
        Game game = new Game(supplied);

        supplied.getBoard().setPiece(Position.parse("e2"), null);
        supplied.setActiveColor(Color.BLACK);
        supplied.getCastlingRights().disableBoth(Color.WHITE);

        GameState current = game.getState();
        assertEquals(PieceType.PAWN, current.getBoard()
                .getPiece(Position.parse("e2")).type());
        assertEquals(Color.WHITE, current.getActiveColor());
        assertTrue(current.getCastlingRights().canCastle(Color.WHITE, true));
        assertTrue(game.playMove(Move.parse("e2e4")).accepted());
    }

    @Test
    void capturePromotionIsAccepted() {
        Board board = Board.empty();
        put(board, "h1", Color.WHITE, PieceType.KING);
        put(board, "h8", Color.BLACK, PieceType.KING);
        put(board, "a7", Color.WHITE, PieceType.PAWN);
        put(board, "b8", Color.BLACK, PieceType.ROOK);
        Game game = new Game(new GameState(board, Color.WHITE,
                new CastlingRights(false, false, false, false), null, 0, 1, GameStatus.ACTIVE));

        assertTrue(game.playMove(new Move(Position.parse("a7"), Position.parse("b8"),
                PieceType.KNIGHT)).accepted());
        assertEquals(PieceType.KNIGHT, game.getState().getBoard()
                .getPiece(Position.parse("b8")).type());
        assertNull(game.getState().getBoard().getPiece(Position.parse("a7")));
    }

    private String boardKey(Board board) {
        StringBuilder key = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                key.append(piece == null ? '.' : piece.displaySymbol());
            }
        }
        return key.toString();
    }

    private void put(Board board, String square, Color color, PieceType type) {
        board.setPiece(Position.parse(square), new Piece(color, type));
    }
}
