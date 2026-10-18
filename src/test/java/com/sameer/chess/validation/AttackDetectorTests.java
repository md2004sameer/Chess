package com.sameer.chess.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sameer.chess.board.Board;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;

class AttackDetectorTests {

    private final AttackDetector detector = new AttackDetector();

    @Test
    void detectsPawnDiagonalAttackOnEmptySquare() {
        Board board = new Board();
        place(board, Color.BLACK, Type.PAWN, 3, 3);

        assertTrue(detector.isSquareAttacked(board, new Position(4, 2), Color.BLACK));
        assertFalse(detector.isSquareAttacked(board, new Position(4, 3), Color.BLACK));
    }

    @Test
    void slidingPieceAttackStopsAtFirstBlocker() {
        Board board = new Board();
        place(board, Color.BLACK, Type.ROOK, 0, 4);
        place(board, Color.WHITE, Type.PAWN, 3, 4);

        assertTrue(detector.isSquareAttacked(board, new Position(2, 4), Color.BLACK));
        assertFalse(detector.isSquareAttacked(board, new Position(4, 4), Color.BLACK));
    }

    @Test
    void detectsWhenTheWhiteKingIsInCheck() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.BLACK, Type.ROOK, 0, 4);

        assertTrue(detector.isSquareAttacked(board, board.findKing(Color.WHITE), Color.BLACK));
    }

    private Piece place(Board board, Color color, Type type, int row, int col) {
        Piece piece = new Piece(color, type);
        board.placePiece(new Position(row, col), piece);
        return piece;
    }
}
