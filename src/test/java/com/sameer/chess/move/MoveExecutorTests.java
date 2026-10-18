package com.sameer.chess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.sameer.chess.TestBoards.place;

import org.junit.jupiter.api.Test;

import com.sameer.chess.board.Board;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.game.GameState;

class MoveExecutorTests {

    private final MoveExecutor executor = new MoveExecutor();

    @Test
    void executesAndUndoesCapture() {
        Board board = new Board();
        Position from = new Position(7, 0);
        Position to = new Position(0, 0);
        Piece rook = place(board, Color.WHITE, Type.ROOK, from);
        Piece queen = place(board, Color.BLACK, Type.QUEEN, to);

        Move request = new Move(from, to);
        MoveExecutor.Executed executed = executor.execute(board, request);

        assertSame(request, executed.move());
        assertSame(queen, executed.captured());
        assertSame(rook, board.getPiece(to));
        executor.undo(board, executed);
        assertSame(rook, board.getPiece(from));
        assertSame(queen, board.getPiece(to));
    }

    @Test
    void undoRejectsWhenDestinationDoesNotContainTheMovedPiece() {
        Board board = new Board();
        Position from = new Position(7, 0);
        Position to = new Position(0, 0);
        Piece rook = place(board, Color.WHITE, Type.ROOK, from);
        MoveExecutor.Executed executed = executor.execute(board, new Move(from, to));
        board.removePiece(to);

        assertThrows(IllegalStateException.class, () -> executor.undo(board, executed));
        assertNull(board.getPiece(to));
    }

    @Test
    void undoRestoresUnexpectedDestinationPieceBeforeThrowing() {
        Board board = new Board();
        Position from = new Position(7, 0);
        Position to = new Position(0, 0);
        Piece rook = place(board, Color.WHITE, Type.ROOK, from);
        MoveExecutor.Executed executed = executor.execute(board, new Move(from, to));
        Piece unexpected = new Piece(Color.BLACK, Type.BISHOP);
        board.removePiece(to);
        board.placePiece(to, unexpected);

        assertThrows(IllegalStateException.class, () -> executor.undo(board, executed));
        assertSame(unexpected, board.getPiece(to));
        assertNull(board.getPiece(from));
    }

    @Test
    void undoRejectsOccupiedOriginWithoutMutatingBoard() {
        Board board = new Board();
        Position from = new Position(7, 0);
        Position to = new Position(6, 0);
        Piece rook = place(board, Color.WHITE, Type.ROOK, from);
        MoveExecutor.Executed executed = executor.execute(board, new Move(from, to));
        Piece obstruction = place(board, Color.BLACK, Type.BISHOP, from);

        assertThrows(IllegalStateException.class, () -> executor.undo(board, executed));
        assertSame(rook, board.getPiece(to));
        assertSame(obstruction, board.getPiece(from));
    }

    @Test
    void executionRejectsMissingSourceAndNullArguments() {
        Board board = new Board();
        Move move = new Move(new Position(4, 4), new Position(4, 5));
        Piece rook = place(board, Color.WHITE, Type.ROOK, new Position(7, 0));
        MoveExecutor.Executed executed = executor.execute(
                board, new Move(new Position(7, 0), new Position(6, 0)));

        assertThrows(NullPointerException.class, () -> executor.execute(null, move));
        assertThrows(NullPointerException.class, () -> executor.execute(board, null));
        assertThrows(IllegalArgumentException.class, () -> executor.execute(board, move));
        assertThrows(NullPointerException.class, () -> executor.undo(null, executed));
        assertThrows(NullPointerException.class, () -> executor.undo(board, null));
    }

    @Test
    void executedRequiresMovedPiece() {
        Move move = new Move(new Position(4, 4), new Position(4, 5));

        assertThrows(NullPointerException.class, () -> new MoveExecutor.Executed(move, null, null));
    }

    @Test
    void executionRejectsSameSourceAndDestinationWithoutMutatingBoard() {
        Board board = new Board();
        Position position = new Position(4, 4);
        Piece rook = place(board, Color.WHITE, Type.ROOK, position);

        assertThrows(IllegalArgumentException.class, () -> executor.execute(board, new Move(position, position)));
        assertSame(rook, board.getPiece(position));
    }

    @Test
    void executionRejectsCapturingOwnPieceWithoutMutatingBoard() {
        Board board = new Board();
        Position from = new Position(4, 4);
        Position to = new Position(4, 5);
        Piece rook = place(board, Color.WHITE, Type.ROOK, from);
        Piece bishop = place(board, Color.WHITE, Type.BISHOP, to);

        assertThrows(IllegalArgumentException.class, () -> executor.execute(board, new Move(from, to)));
        assertSame(rook, board.getPiece(from));
        assertSame(bishop, board.getPiece(to));
    }

    @Test
    void executionRejectsCapturingKingWithoutMutatingBoard() {
        Board board = new Board();
        Position from = new Position(4, 4);
        Position to = new Position(4, 5);
        Piece rook = place(board, Color.WHITE, Type.ROOK, from);
        Piece king = place(board, Color.BLACK, Type.KING, to);

        assertThrows(IllegalArgumentException.class, () -> executor.execute(board, new Move(from, to)));
        assertSame(rook, board.getPiece(from));
        assertSame(king, board.getPiece(to));
    }

    @Test
    void executionRejectsEnPassantWithoutCapturedPieceWithoutMutatingBoard() {
        Board board = new Board();
        Position from = new Position(3, 4);
        Position to = new Position(2, 3);
        Piece pawn = place(board, Color.WHITE, Type.PAWN, from);
        Move enPassant = new Move(from, to, null, MoveKind.EN_PASSANT);

        assertThrows(IllegalArgumentException.class, () -> executor.execute(board, enPassant));
        assertSame(pawn, board.getPiece(from));
        assertNull(board.getPiece(to));
    }

    @Test
    void executesAndUndoesPromotion() {
        Board board = new Board();
        Position from = new Position(1, 0);
        Position to = new Position(0, 0);
        Piece pawn = place(board, Color.WHITE, Type.PAWN, from);
        Move promotion = new Move(from, to, Type.QUEEN, MoveKind.PROMOTION);

        MoveExecutor.Executed executed = executor.execute(board, promotion);

        assertSame(pawn, executed.moved());
        assertEquals(Type.QUEEN, board.getPiece(to).getType());
        executor.undo(board, executed);
        assertSame(pawn, board.getPiece(from));
        assertNull(board.getPiece(to));
    }

    @Test
    void executesAndUndoesPromotionCapture() {
        Board board = new Board();
        Position from = new Position(1, 0);
        Position to = new Position(0, 1);
        Piece pawn = place(board, Color.WHITE, Type.PAWN, from);
        Piece captured = place(board, Color.BLACK, Type.ROOK, to);
        Move promotion = new Move(from, to, Type.KNIGHT, MoveKind.PROMOTION);

        MoveExecutor.Executed executed = executor.execute(board, promotion);

        assertSame(pawn, executed.moved());
        assertSame(captured, executed.captured());
        assertEquals(Type.KNIGHT, board.getPiece(to).getType());
        executor.undo(board, executed);
        assertSame(pawn, board.getPiece(from));
        assertSame(captured, board.getPiece(to));
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
        MoveExecutor.Executed executedDoubleStep = executor.execute(board, doubleStep);
        state.recordMove(doubleStep, blackPawn, null);
        Move enPassant = new Move(
                whitePosition, new Position(2, 3), null, MoveKind.EN_PASSANT);

        MoveExecutor.Executed executed = executor.execute(board, enPassant);

        assertSame(blackPawn, executed.captured());
        assertNull(board.getPiece(blackEnd));
        executor.undo(board, executed);
        assertSame(blackPawn, board.getPiece(blackEnd));
        assertSame(whitePawn, board.getPiece(whitePosition));
        executor.undo(board, executedDoubleStep);
        assertSame(blackPawn, board.getPiece(blackStart));
    }

    @Test
    void undoRejectsOccupiedEnPassantCaptureSquareWithoutMutatingBoard() {
        Board board = new Board();
        Position whitePosition = new Position(3, 4);
        Position blackEnd = new Position(3, 3);
        Position enPassantTo = new Position(2, 3);
        Piece whitePawn = place(board, Color.WHITE, Type.PAWN, whitePosition);
        Piece blackPawn = place(board, Color.BLACK, Type.PAWN, blackEnd);
        Move enPassant = new Move(whitePosition, enPassantTo, null, MoveKind.EN_PASSANT);
        MoveExecutor.Executed executed = executor.execute(board, enPassant);
        Piece obstruction = place(board, Color.BLACK, Type.BISHOP, blackEnd);

        assertThrows(IllegalStateException.class, () -> executor.undo(board, executed));
        assertSame(whitePawn, board.getPiece(enPassantTo));
        assertSame(obstruction, board.getPiece(blackEnd));
    }

    @Test
    void executesAndUndoesBothCastlingSidesForBothColors() {
        for (Color color : Color.values()) {
            for (boolean kingSide : new boolean[]{true, false}) {
                Board board = new Board();
                int row = color.homeRow();
                Position kingFrom = new Position(row, 4);
                Position rookFrom = new Position(row, kingSide ? 7 : 0);
                Position kingTo = new Position(row, kingSide ? 6 : 2);
                Position rookTo = new Position(row, kingSide ? 5 : 3);
                Piece king = place(board, color, Type.KING, kingFrom);
                Piece rook = place(board, color, Type.ROOK, rookFrom);
                Move castle = new Move(kingFrom, kingTo, null, MoveKind.CASTLING);

                MoveExecutor.Executed executed = executor.execute(board, castle);

                assertSame(king, board.getPiece(kingTo));
                assertSame(rook, board.getPiece(rookTo));
                executor.undo(board, executed);
                assertSame(king, board.getPiece(kingFrom));
                assertSame(rook, board.getPiece(rookFrom));
                assertNull(board.getPiece(kingTo));
                assertNull(board.getPiece(rookTo));
            }
        }
    }

    @Test
    void undoRejectsOccupiedCastlingRookStartWithoutMutatingBoard() {
        Board board = new Board();
        Position kingFrom = new Position(7, 4);
        Position kingTo = new Position(7, 6);
        Position rookDestination = new Position(7, 5);
        Position rookStart = new Position(7, 7);
        Piece king = place(board, Color.WHITE, Type.KING, kingFrom);
        Piece rook = place(board, Color.WHITE, Type.ROOK, rookStart);
        Move castle = new Move(kingFrom, kingTo, null, MoveKind.CASTLING);
        MoveExecutor.Executed executed = executor.execute(board, castle);
        Piece obstruction = place(board, Color.BLACK, Type.BISHOP, rookStart);

        assertThrows(IllegalStateException.class, () -> executor.undo(board, executed));
        assertSame(king, board.getPiece(kingTo));
        assertSame(rook, board.getPiece(rookDestination));
        assertSame(obstruction, board.getPiece(rookStart));
    }
}
