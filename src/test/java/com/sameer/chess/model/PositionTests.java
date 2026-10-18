package com.sameer.chess.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PositionTests {

    @Test
    void positionsUseValueEqualityHashingAndRecordStringRepresentation() {
        Position position = new Position(3, 5);
        Position equalPosition = new Position(3, 5);

        assertEquals(position, equalPosition);
        assertEquals(position.hashCode(), equalPosition.hashCode());
        assertNotEquals(position, new Position(3, 4));
        assertEquals("Position[row=3, col=5]", position.toString());
    }

    @Test
    void rejectsCoordinatesOutsideTheBoard() {
        assertThrows(IllegalArgumentException.class, () -> new Position(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, 8));
    }

    @Test
    void boardCheckAcceptsOnlyValidCoordinates() {
        assertTrue(Position.isOnBoard(0, 0));
        assertTrue(Position.isOnBoard(7, 7));
        assertFalse(Position.isOnBoard(-1, 0));
        assertFalse(Position.isOnBoard(0, 8));
    }
}
