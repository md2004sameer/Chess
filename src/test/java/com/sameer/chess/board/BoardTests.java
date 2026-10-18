package com.sameer.chess.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;

class BoardTests {

    @Test
    void storesAndQueriesPiecePlacementThroughReaderAndWriterContracts() {
        Board board = new Board();
        BoardReader reader = board;
        BoardWriter writer = board;
        Position square = new Position(7, 0);
        Piece king = new Piece(Color.WHITE, Type.KING);

        writer.placePiece(square, king);

        assertSame(king, reader.getPiece(square));
        assertTrue(reader.isOccupied(square));
        assertTrue(reader.isInBounds(7, 7));
        assertFalse(reader.isInBounds(8, 7));
        assertEquals(List.of(square), reader.getOccupiedPositions());
        assertEquals(square, reader.findKing(Color.WHITE));
    }

    @Test
    void removesAndMovesPiecesWithoutOwningMoveRules() {
        Board board = new Board();
        Position from = new Position(6, 4);
        Position to = new Position(4, 4);
        Piece pawn = new Piece(Color.WHITE, Type.PAWN);
        board.placePiece(from, pawn);

        board.movePiece(from, to);

        assertNull(board.getPiece(from));
        assertSame(pawn, board.getPiece(to));
        assertSame(pawn, board.removePiece(to));
        assertNull(board.removePiece(to));
        assertTrue(board.getOccupiedPositions().isEmpty());
    }

    @Test
    void rejectsOccupiedSquaresAndInvalidRelocation() {
        Board board = new Board();
        Position from = new Position(7, 0);
        Position occupied = new Position(7, 7);
        Piece rook = new Piece(Color.WHITE, Type.ROOK);
        Piece bishop = new Piece(Color.BLACK, Type.BISHOP);
        board.placePiece(from, rook);
        board.placePiece(occupied, bishop);

        assertThrows(IllegalArgumentException.class,
                () -> board.placePiece(new Position(7, 0), new Piece(Color.WHITE, Type.ROOK)));
        assertThrows(IllegalArgumentException.class,
                () -> board.movePiece(from, new Position(7, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> board.movePiece(from, occupied));
        assertThrows(IllegalArgumentException.class,
                () -> board.movePiece(new Position(6, 0), new Position(5, 0)));
    }

    @Test
    void copyHasIndependentPieceLocations() {
        Board original = new Board();
        Position from = new Position(7, 1);
        Position to = new Position(5, 2);
        Piece knight = new Piece(Color.WHITE, Type.KNIGHT);
        Position blackKingSquare = new Position(0, 4);
        Piece blackKing = new Piece(Color.BLACK, Type.KING);
        original.placePiece(from, knight);
        original.placePiece(blackKingSquare, blackKing);
        Board copy = original.copy();

        assertSame(knight, copy.getPiece(from));
        assertSame(blackKing, copy.getPiece(blackKingSquare));
        assertEquals(List.of(new Position(0, 4), from), copy.getOccupiedPositions());
        assertNull(copy.findKing(Color.WHITE));
        assertEquals(blackKingSquare, copy.findKing(Color.BLACK));
        copy.movePiece(from, to);

        assertNotNull(original.getPiece(from));
        assertNull(original.getPiece(to));
        assertNull(copy.getPiece(from));
        assertEquals(Type.KNIGHT, copy.getPiece(to).getType());
    }

    @Test
    void findKingReturnsNullWhenColorHasNoKing() {
        Board board = new Board();
        board.placePiece(new Position(4, 4), new Piece(Color.BLACK, Type.KNIGHT));

        assertNull(board.findKing(Color.WHITE));
        assertNull(board.findKing(Color.BLACK));
    }
}
