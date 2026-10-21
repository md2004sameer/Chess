package com.sameer.chess.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ColorTests {
    @Test
    void returnsTheOppositeColor() {
        assertEquals(Color.BLACK, Color.WHITE.opposite());
        assertEquals(Color.WHITE, Color.BLACK.opposite());
    }
}
