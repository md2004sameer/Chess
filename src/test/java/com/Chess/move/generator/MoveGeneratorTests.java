package com.Chess.move.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.Chess.board.Board;
import com.Chess.model.Piece;
import com.Chess.model.Position;
import com.Chess.model.enums.Color;
import com.Chess.model.enums.Type;
import com.Chess.move.Move;

class MoveGeneratorTests {

    @Test
    void rookStopsAtPiecesAndCanCaptureOpponent() {
        Board board = new Board();
        Piece rook = place(board, Color.WHITE, Type.ROOK, 4, 3);
        place(board, Color.WHITE, Type.PAWN, 4, 5);
        place(board, Color.BLACK, Type.PAWN, 2, 3);

        List<Move> moves = new RookMoveGenerator().generateMoves(rook, board);

        assertTrue(hasMove(moves, 4, 3, 2, 3));
        assertTrue(!hasMove(moves, 4, 3, 4, 5));
        assertTrue(!hasMove(moves, 4, 3, 4, 6));
    }

    @Test
    void bishopMovesDiagonallyAndStopsAtBlocker() {
        Board board = new Board();
        Piece bishop = place(board, Color.WHITE, Type.BISHOP, 4, 3);
        place(board, Color.BLACK, Type.PAWN, 2, 1);

        List<Move> moves = new BishopMoveGenerator().generateMoves(bishop, board);

        assertTrue(hasMove(moves, 4, 3, 2, 1));
        assertTrue(!hasMove(moves, 4, 3, 1, 0));
        assertTrue(hasMove(moves, 4, 3, 3, 2));
        assertTrue(!hasMove(moves, 4, 3, 3, 3));
    }

    @Test
    void queenCombinesRookAndBishopDirections() {
        Board board = new Board();
        Piece queen = place(board, Color.WHITE, Type.QUEEN, 3, 3);

        List<Move> moves = new QueenMoveGenerator().generateMoves(queen, board);

        assertTrue(hasMove(moves, 3, 3, 3, 7));
        assertTrue(hasMove(moves, 3, 3, 7, 7));
        assertEquals(27, moves.size());
    }

    @Test
    void kingMovesOneSquareAndCannotLandOnFriendlyPiece() {
        Board board = new Board();
        Piece king = place(board, Color.WHITE, Type.KING, 0, 0);
        place(board, Color.WHITE, Type.PAWN, 0, 1);

        List<Move> moves = new KingMoveGenerator().generateMoves(king, board);

        assertEquals(2, moves.size());
        assertTrue(hasMove(moves, 0, 0, 1, 0));
        assertTrue(hasMove(moves, 0, 0, 1, 1));
        assertTrue(!hasMove(moves, 0, 0, 0, 1));
    }

    @Test
    void whitePawnCanAdvanceOneOrTwoSquaresWhenClear() {
        Board board = new Board();
        Piece pawn = place(board, Color.WHITE, Type.PAWN, 6, 3);

        List<Move> moves = new PawnMoveGenerator().generateMoves(pawn, board);

        assertEquals(2, moves.size());
        assertTrue(hasMove(moves, 6, 3, 5, 3));
        assertTrue(hasMove(moves, 6, 3, 4, 3));
    }

    @Test
    void pawnCapturesOnlyOpposingPiecesDiagonally() {
        Board board = new Board();
        Piece pawn = place(board, Color.BLACK, Type.PAWN, 3, 3);
        place(board, Color.WHITE, Type.PAWN, 4, 2);
        place(board, Color.BLACK, Type.PAWN, 4, 4);
        place(board, Color.BLACK, Type.PAWN, 4, 3);

        List<Move> moves = new PawnMoveGenerator().generateMoves(pawn, board);

        assertTrue(hasMove(moves, 3, 3, 4, 2));
        assertTrue(!hasMove(moves, 3, 3, 4, 4));
        assertTrue(!hasMove(moves, 3, 3, 4, 3));
    }

    @Test
    void knightMovesInLShape() {
        Board board = new Board();
        Piece knight = place(board, Color.WHITE, Type.KNIGHT, 0, 0);

        List<Move> moves = new KnightMoveGenerator().generateMoves(knight, board);

        assertEquals(2, moves.size());
        assertTrue(hasMove(moves, 0, 0, 1, 2));
        assertTrue(hasMove(moves, 0, 0, 2, 1));
    }

    private Piece place(Board board, Color color, Type type, int row, int col) {
        Piece piece = new Piece(color, type, new Position(row, col));
        board.placePiece(piece);
        return piece;
    }

    private boolean hasMove(List<Move> moves, int fromRow, int fromCol, int toRow, int toCol) {
        return moves.stream().anyMatch(move ->
                move.getFrom().getRow() == fromRow
                        && move.getFrom().getCol() == fromCol
                        && move.getTo().getRow() == toRow
                        && move.getTo().getCol() == toCol);
    }
}
