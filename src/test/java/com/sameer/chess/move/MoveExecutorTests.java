package com.sameer.chess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;

class MoveExecutorTests {

    private final MoveExecutor executor = new MoveExecutor();

    @Test
    void executesAndUndoesCapture() {
        Board board = new Board();
        Position from = new Position(7, 0);
        Position to = new Position(0, 0);
        Piece rook = place(board, Color.WHITE, Type.ROOK, from);
        Piece queen = place(board, Color.BLACK, Type.QUEEN, to);

        Move executed = executor.execute(board, board, new Move(from, to), new GameState(Color.WHITE));

        assertSame(queen, executed.capturedPiece());
        assertSame(rook, board.getPiece(to));
        executor.undo(board, executed, rook);
        assertSame(rook, board.getPiece(from));
        assertSame(queen, board.getPiece(to));
    }

    @Test
    void executesAndUndoesPromotion() {
        Board board = new Board();
        Position from = new Position(1, 0);
        Position to = new Position(0, 0);
        Piece pawn = place(board, Color.WHITE, Type.PAWN, from);
        Move promotion = new Move(from, to, null, Type.QUEEN, MoveKind.PROMOTION);

        executor.execute(board, board, promotion, new GameState(Color.WHITE));

        assertEquals(Type.QUEEN, board.getPiece(to).getType());
        executor.undo(board, promotion, pawn);
        assertSame(pawn, board.getPiece(from));
        assertTrue(board.getPiece(to) == null);
    }

    @Test
    void executesAndUndoesEnPassant() {
        Board board = new Board();
        Position whitePosition = new Position(3, 4);
        Position blackStart = new Position(1, 3);
        Position blackEnd = new Position(3, 3);
        Piece whitePawn = place(board, Color.WHITE, Type.PAWN, whitePosition);
        Piece blackPawn = place(board, Color.BLACK, Type.PAWN, blackStart);
        GameState state = new GameState(Color.BLACK);
        Move doubleStep = new Move(blackStart, blackEnd);
        executor.execute(board, board, doubleStep, state);
        state.recordMove(doubleStep, blackPawn, null);
        Move enPassant = new Move(
                whitePosition, new Position(2, 3), null, null, MoveKind.EN_PASSANT);

        Move executed = executor.execute(board, board, enPassant, state);

        assertSame(blackPawn, executed.capturedPiece());
        assertTrue(board.getPiece(blackEnd) == null);
        executor.undo(board, executed, whitePawn);
        assertSame(blackPawn, board.getPiece(blackEnd));
        assertSame(whitePawn, board.getPiece(whitePosition));
        executor.undo(board, doubleStep, blackPawn);
        assertSame(blackPawn, board.getPiece(blackStart));
    }

    @Test
    void executesAndUndoesCastling() {
        Board board = new Board();
        Position kingFrom = new Position(7, 4);
        Position rookFrom = new Position(7, 7);
        Piece king = place(board, Color.WHITE, Type.KING, kingFrom);
        Piece rook = place(board, Color.WHITE, Type.ROOK, rookFrom);
        GameState state = new GameState(Color.WHITE);
        state.setCastlingRights(Color.WHITE, true, false);
        Move castle = new Move(
                kingFrom, new Position(7, 6), null, null, MoveKind.CASTLING);

        executor.execute(board, board, castle, state);

        assertSame(king, board.getPiece(new Position(7, 6)));
        assertSame(rook, board.getPiece(new Position(7, 5)));
        executor.undo(board, castle, king);
        assertSame(king, board.getPiece(kingFrom));
        assertSame(rook, board.getPiece(rookFrom));
    }

    private Piece place(Board board, Color color, Type type, Position position) {
        Piece piece = new Piece(color, type);
        board.placePiece(position, piece);
        return piece;
    }
}
