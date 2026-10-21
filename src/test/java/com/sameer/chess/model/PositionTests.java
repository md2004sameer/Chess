package com.sameer.chess.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PositionTests {
    @Test
    void parsesAndFormatsBoardCoordinates() {
        Position position = Position.parse("e2");

        assertEquals(new Position(6, 4), position);
        assertEquals("e2", position.toString());
        assertTrue(position.isInsideBoard());
    }

    @Test
    void rejectsInvalidSquares() {
        assertThrows(IllegalArgumentException.class, () -> Position.parse(null));
        assertThrows(IllegalArgumentException.class, () -> Position.parse("E2"));
        assertThrows(IllegalArgumentException.class, () -> Position.parse("e9"));
        assertFalse(new Position(8, 0).isInsideBoard());
    }
}
