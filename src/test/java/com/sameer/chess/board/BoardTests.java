package com.sameer.chess.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        Piece rook = new Piece(Color.WHITE, Type.KING);

        writer.placePiece(square, rook);

        assertSame(rook, reader.getPiece(square));
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

        assertTrue(board.getPiece(from) == null);
        assertSame(pawn, board.getPiece(to));
        assertSame(pawn, board.removePiece(to));
        assertTrue(board.getOccupiedPositions().isEmpty());
    }

    @Test
    void rejectsOccupiedSquaresDuplicatePiecesAndInvalidRelocation() {
        Board board = new Board();
        Position from = new Position(7, 0);
        Piece rook = new Piece(Color.WHITE, Type.ROOK);
        board.placePiece(from, rook);

        assertThrows(IllegalArgumentException.class,
                () -> board.placePiece(new Position(7, 0), new Piece(Color.WHITE, Type.ROOK)));
        assertThrows(IllegalArgumentException.class,
                () -> board.placePiece(new Position(7, 7), rook));
        assertThrows(IllegalArgumentException.class,
                () -> board.movePiece(from, new Position(7, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> board.movePiece(new Position(6, 0), new Position(5, 0)));
    }

    @Test
    void copyHasIndependentPieceLocations() {
        Board original = new Board();
        Position from = new Position(7, 1);
        Position to = new Position(5, 2);
        original.placePiece(from, new Piece(Color.WHITE, Type.KNIGHT));
        Board copy = original.copy();

        copy.movePiece(from, to);

        assertFalse(original.getPiece(from) == null);
        assertTrue(original.getPiece(to) == null);
        assertTrue(copy.getPiece(from) == null);
        assertEquals(Type.KNIGHT, copy.getPiece(to).getType());
    }
}
