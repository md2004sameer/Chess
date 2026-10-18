package com.sameer.chess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Type;

class MoveTests {

    @Test
    void basicMoveHasNormalKindAndNoOptionalPromotion() {
        Move move = new Move(new Position(6, 4), new Position(4, 4));

        assertTrue(move.getPromotionType().isEmpty());
        assertEquals(MoveKind.NORMAL, move.kind());
    }

    @Test
    void storesPromotionAndSpecialMoveKinds() {
        Move promotion = new Move(
                new Position(1, 0), new Position(0, 0), Type.KNIGHT, MoveKind.PROMOTION);
        Move castle = new Move(
                new Position(7, 4), new Position(7, 6), null, MoveKind.CASTLING);
        Move enPassant = new Move(
                new Position(3, 4), new Position(2, 3), null, MoveKind.EN_PASSANT);

        assertEquals(Type.KNIGHT, promotion.promotionType());
        assertTrue(castle.isCastling());
        assertTrue(enPassant.isEnPassant());
    }

    @Test
    void rejectsInvalidPromotionMetadata() {
        assertThrows(IllegalArgumentException.class, () -> new Move(
                new Position(1, 0), new Position(0, 0), Type.KING, MoveKind.PROMOTION));
        assertThrows(IllegalArgumentException.class, () -> new Move(
                new Position(3, 4), new Position(2, 3), Type.QUEEN, MoveKind.EN_PASSANT));
        assertThrows(IllegalArgumentException.class, () -> new Move(
                new Position(1, 0), new Position(0, 0), null, MoveKind.PROMOTION));
    }
}
