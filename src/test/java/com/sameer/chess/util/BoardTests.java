package com.sameer.chess.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

class BoardTests {
    @Test
    void initialPositionPlacesAllThirtyTwoPiecesOnTheExpectedSquares() {
        Board board = Board.initialPosition();
        int pieceCount = 0;
        PieceType[] backRank = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (int col = 0; col < 8; col++) {
            assertPiece(board, 0, col, Color.BLACK, backRank[col]);
            assertPiece(board, 1, col, Color.BLACK, PieceType.PAWN);
            assertPiece(board, 6, col, Color.WHITE, PieceType.PAWN);
            assertPiece(board, 7, col, Color.WHITE, backRank[col]);
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board.getPiece(new Position(row, col)) != null) pieceCount++;
            }
        }

        assertEquals(32, pieceCount);
    }

    @Test
    void boardCopyHasIndependentSquares() {
        Board original = Board.initialPosition();
        Board copy = original.copy();

        assertNotSame(original, copy);
        assertSame(original.getPiece(Position.parse("e2")),
                copy.getPiece(Position.parse("e2")));
        copy.setPiece(Position.parse("e2"), null);
        copy.setPiece(Position.parse("e4"), new Piece(Color.WHITE, PieceType.PAWN));

        assertEquals(PieceType.PAWN, original.getPiece(Position.parse("e2")).type());
        assertNull(original.getPiece(Position.parse("e4")));
    }

    private void assertPiece(Board board, int row, int col, Color color, PieceType type) {
        Piece piece = board.getPiece(new Position(row, col));
        assertEquals(new Piece(color, type), piece);
    }
}
