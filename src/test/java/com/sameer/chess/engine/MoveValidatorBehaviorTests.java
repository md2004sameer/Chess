package com.sameer.chess.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.CastlingRights;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;
import com.sameer.chess.movegen.MoveGenerator;

class MoveValidatorBehaviorTests {
    private final MoveValidator validator = MoveValidator.standard(
            new MoveGenerator(), new MoveExecutor(), new AttackDetector());

    @Test
    void coordinatesOffBoardRejected() {
        assertFalse(validator.validate(state(emptyBoard()), move(-1, 0, 0, 0)).valid());
    }

    @Test
    void emptySourceRejected() {
        assertFalse(validator.validate(state(emptyBoard()), move(4, 4, 4, 5)).valid());
    }

    @Test
    void wrongTurnRejected() {
        Board board = emptyBoard();
        put(board, "a8", Color.BLACK, PieceType.KNIGHT);

        assertFalse(validator.validate(state(board), Move.parse("a8b6")).valid());
    }

    @Test
    void promotionRequiredWhenPawnReachesLastRow() {
        Board board = emptyBoard();
        put(board, "a7", Color.WHITE, PieceType.PAWN);

        var result = validator.validate(state(board), Move.parse("a7a8"));

        assertFalse(result.valid());
        assertEquals("A pawn reaching the last rank must be promoted.", result.message());
    }

    @Test
    void promotionForbiddenOnNormalMove() {
        Board board = emptyBoard();
        put(board, "a2", Color.WHITE, PieceType.PAWN);

        assertFalse(validator.validate(state(board),
                new Move(Position.parse("a2"), Position.parse("a3"), PieceType.QUEEN)).valid());
    }

    @Test
    void moveNotInPatternRejected() {
        Board board = emptyBoard();
        put(board, "a2", Color.WHITE, PieceType.PAWN);

        assertFalse(validator.validate(state(board), Move.parse("a2a5")).valid());
    }

    @Test
    void pinnedPieceCannotMove() {
        Board board = emptyBoard();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "e2", Color.WHITE, PieceType.ROOK);
        put(board, "e8", Color.BLACK, PieceType.ROOK);

        assertFalse(validator.validate(state(board), Move.parse("e2d2")).valid());
    }

    @Test
    void kingCannotMoveIntoCheck() {
        Board board = emptyBoard();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "a8", Color.BLACK, PieceType.KING);
        put(board, "e8", Color.BLACK, PieceType.ROOK);

        assertFalse(validator.validate(state(board), Move.parse("e1e2")).valid());
    }

    @Test
    void checkMustBeAnswered() {
        Board board = emptyBoard();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "a8", Color.BLACK, PieceType.KING);
        put(board, "e8", Color.BLACK, PieceType.ROOK);
        put(board, "a2", Color.WHITE, PieceType.ROOK);

        assertFalse(validator.validate(state(board), Move.parse("a2a3")).valid());
        assertTrue(validator.validate(state(board), Move.parse("a2e2")).valid());
    }

    @Test
    void enPassantExposingKingRejected() {
        Board board = emptyBoard();
        put(board, "h5", Color.WHITE, PieceType.KING);
        put(board, "h8", Color.BLACK, PieceType.KING);
        put(board, "a5", Color.BLACK, PieceType.ROOK);
        put(board, "f5", Color.WHITE, PieceType.PAWN);
        put(board, "g5", Color.BLACK, PieceType.PAWN);
        GameState state = new GameState(board, Color.WHITE, noRights(),
                Position.parse("g6"), 0, 1, GameStatus.ACTIVE);

        assertFalse(validator.validate(state, Move.parse("f5g6")).valid());
    }

    private GameState state(Board board) {
        return new GameState(board, Color.WHITE, noRights(), null, 0, 1, GameStatus.ACTIVE);
    }

    private Board emptyBoard() {
        Board board = Board.empty();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "a8", Color.BLACK, PieceType.KING);
        return board;
    }

    private CastlingRights noRights() {
        return new CastlingRights(false, false, false, false);
    }

    private Move move(int fromRow, int fromCol, int toRow, int toCol) {
        return new Move(new Position(fromRow, fromCol), new Position(toRow, toCol), null);
    }

    private void put(Board board, String square, Color color, PieceType type) {
        board.setPiece(Position.parse(square), new Piece(color, type));
    }
}
