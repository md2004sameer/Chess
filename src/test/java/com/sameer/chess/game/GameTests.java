package com.sameer.chess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sameer.chess.board.Board;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;
import com.sameer.chess.move.MoveExecutor;
import com.sameer.chess.move.generator.MoveGeneratorConfiguration;
import com.sameer.chess.move.rule.CastlingRule;
import com.sameer.chess.move.rule.EnPassantRule;
import com.sameer.chess.move.rule.PromotionRule;
import com.sameer.chess.validation.AttackDetector;
import com.sameer.chess.validation.MoveValidator;

class GameTests {

    @Test
    void playAndUndoMaintainSideAndMoveClocks() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.BLACK, Type.KING, 0, 4);
        place(board, Color.WHITE, Type.KNIGHT, 7, 1);
        place(board, Color.BLACK, Type.KNIGHT, 0, 1);
        Game game = game(board, new GameState(Color.WHITE));

        game.playMove(move(7, 1, 5, 2));

        assertEquals(Color.BLACK, game.getState().getSideToMove());
        assertEquals(1, game.getState().getHalfMoveClock());
        assertEquals(1, game.getState().getFullMoveNumber());
        assertEquals(1, game.getState().getMoveHistory().size());

        game.playMove(move(0, 1, 2, 2));

        assertEquals(Color.WHITE, game.getState().getSideToMove());
        assertEquals(2, game.getState().getHalfMoveClock());
        assertEquals(2, game.getState().getFullMoveNumber());
        assertEquals(2, game.getState().getMoveHistory().size());

        game.undoMove();
        assertEquals(Color.BLACK, game.getState().getSideToMove());
        assertEquals(1, game.getState().getHalfMoveClock());
        assertEquals(1, game.getState().getFullMoveNumber());
        assertEquals(1, game.getState().getMoveHistory().size());
        assertNull(board.getPiece(new Position(2, 2)));
    }

    @Test
    void pawnDoubleStepCreatesOneReplyEnPassantOpportunityAndUndoRestoresIt() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 7);
        place(board, Color.BLACK, Type.KING, 0, 7);
        place(board, Color.WHITE, Type.PAWN, 3, 4);
        place(board, Color.BLACK, Type.PAWN, 1, 3);
        Game game = game(board, new GameState(Color.BLACK));

        game.playMove(move(1, 3, 3, 3));

        assertEquals(new Position(2, 3), game.getState().getEnPassantTarget());
        Move enPassant = game.getLegalMoves(new Position(3, 4)).stream()
                .filter(move -> move.isEnPassant())
                .findFirst()
                .orElseThrow();
        game.playMove(enPassant);

        assertTrue(game.getState().getEnPassantTarget() == null);
        assertEquals(0, game.getState().getHalfMoveClock());

        game.undoMove();
        assertEquals(Color.WHITE, game.getState().getSideToMove());
        assertEquals(new Position(2, 3), game.getState().getEnPassantTarget());

        game.playMove(move(7, 7, 6, 7));
        assertTrue(game.getState().getEnPassantTarget() == null);
    }

    @Test
    void movingKingOrCapturingHomeRookUpdatesCastlingRightsAndUndoRestoresThem() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.WHITE, Type.ROOK, 7, 7);
        place(board, Color.BLACK, Type.KING, 0, 4);
        place(board, Color.BLACK, Type.ROOK, 0, 7);
        GameState state = new GameState(Color.WHITE);
        state.setCastlingRights(Color.WHITE, true, true);
        Game game = game(board, state);

        game.playMove(move(7, 4, 6, 4));
        assertFalse(state.canCastle(Color.WHITE, true));
        assertFalse(state.canCastle(Color.WHITE, false));
        game.undoMove();
        assertTrue(state.canCastle(Color.WHITE, true));
        assertTrue(state.canCastle(Color.WHITE, false));

        state = new GameState(Color.BLACK);
        state.setCastlingRights(Color.WHITE, true, false);
        Game captureGame = game(board, state);
        captureGame.playMove(move(0, 7, 7, 7));
        assertFalse(state.canCastle(Color.WHITE, true));
        captureGame.undoMove();
        assertTrue(state.canCastle(Color.WHITE, true));
    }

    @Test
    void rejectsIllegalMovesWithoutChangingGameState() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.BLACK, Type.KING, 0, 4);
        Game game = game(board, new GameState(Color.WHITE));

        assertThrows(IllegalArgumentException.class, () -> game.playMove(move(7, 4, 5, 3)));
        assertEquals(Color.WHITE, game.getState().getSideToMove());
        assertEquals(0, game.getState().getHalfMoveClock());
        assertEquals(1, game.getState().getFullMoveNumber());
    }

    private Piece place(Board board, Color color, Type type, int row, int col) {
        Piece piece = new Piece(color, type);
        board.placePiece(new Position(row, col), piece);
        return piece;
    }

    private Move move(int fromRow, int fromCol, int toRow, int toCol) {
        return new Move(new Position(fromRow, fromCol), new Position(toRow, toCol));
    }

    private Game game(Board board, GameState state) {
        MoveValidator validator = new MoveValidator(
                new MoveGeneratorConfiguration().moveGeneratorRegistry(),
                List.of(new CastlingRule(), new EnPassantRule(), new PromotionRule()),
                new AttackDetector(),
                new MoveExecutor());
        return new Game(board, board, state, validator, new MoveExecutor());
    }
}
