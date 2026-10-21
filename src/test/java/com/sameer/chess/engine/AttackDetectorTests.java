package com.sameer.chess.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

class AttackDetectorTests {
    private final AttackDetector detector = new AttackDetector();

    @Test
    void pawnAttacksAnEmptyDiagonalSquare() {
        Board board = Board.empty();
        board.setPiece(Position.parse("e4"), new Piece(Color.BLACK, PieceType.PAWN));

        assertTrue(detector.isSquareAttacked(board, Position.parse("d3"), Color.BLACK));
    }

    @Test
    void pieceBlocksSlidingAttackRay() {
        Board board = Board.empty();
        board.setPiece(Position.parse("a8"), new Piece(Color.BLACK, PieceType.ROOK));
        board.setPiece(Position.parse("a4"), new Piece(Color.WHITE, PieceType.PAWN));

        assertFalse(detector.isSquareAttacked(board, Position.parse("a1"), Color.BLACK));
    }

    @Test
    void adjacentKingAttacksTheNeighboringSquare() {
        Board board = Board.empty();
        board.setPiece(Position.parse("e5"), new Piece(Color.BLACK, PieceType.KING));

        assertTrue(detector.isSquareAttacked(board, Position.parse("d4"), Color.BLACK));
    }

    @Test
    void detectsKnightAndDiagonalSliderAttacks() {
        Board board = Board.empty();
        board.setPiece(Position.parse("c3"), new Piece(Color.BLACK, PieceType.KNIGHT));
        board.setPiece(Position.parse("a8"), new Piece(Color.BLACK, PieceType.BISHOP));
        board.setPiece(Position.parse("h8"), new Piece(Color.BLACK, PieceType.QUEEN));

        assertTrue(detector.isSquareAttacked(board, Position.parse("d5"), Color.BLACK));
        assertTrue(detector.isSquareAttacked(board, Position.parse("d4"), Color.BLACK));
    }

    @Test
    void detectsRookAttacksAlongAnOpenFile() {
        Board board = Board.empty();
        board.setPiece(Position.parse("a8"), new Piece(Color.BLACK, PieceType.ROOK));

        assertTrue(detector.isSquareAttacked(board, Position.parse("a1"), Color.BLACK));
    }
}
