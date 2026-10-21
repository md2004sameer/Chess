package com.sameer.chess.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class MoveTests {
    @Test
    void movesUseValueEqualityIncludingPromotion() {
        Move first = new Move(Position.parse("e7"), Position.parse("e8"), PieceType.QUEEN);
        Move same = new Move(Position.parse("e7"), Position.parse("e8"), PieceType.QUEEN);
        Move differentPromotion = new Move(
                Position.parse("e7"), Position.parse("e8"), PieceType.ROOK);

        assertEquals(first, same);
        assertNotEquals(first, differentPromotion);
    }
}
