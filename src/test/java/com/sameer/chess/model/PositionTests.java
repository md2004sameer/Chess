package com.sameer.chess.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PositionTests {

    @Test
    void rejectsCoordinatesOutsideTheBoard() {
        assertThrows(IllegalArgumentException.class, () -> new Position(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Position(8, 0));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, -1));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, 8));
    }

    @Test
    void boardCheckAcceptsOnlyValidCoordinates() {
        assertTrue(Position.isOnBoard(0, 0));
        assertTrue(Position.isOnBoard(7, 7));
        assertFalse(Position.isOnBoard(-1, 0));
        assertFalse(Position.isOnBoard(8, 0));
        assertFalse(Position.isOnBoard(0, -1));
        assertFalse(Position.isOnBoard(0, 8));
    }
}
