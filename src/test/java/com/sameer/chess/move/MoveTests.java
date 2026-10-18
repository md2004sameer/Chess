package com.sameer.chess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;

class MoveTests {

    @Test
    void basicMoveHasNormalKindAndNoOptionalMetadata() {
        Move move = new Move(new Position(6, 4), new Position(4, 4));

        assertTrue(move.getCapturedPiece().isEmpty());
        assertTrue(move.getPromotionType().isEmpty());
        assertEquals(MoveKind.NORMAL, move.kind());
    }

    @Test
    void movesUseValueEqualityHashingAndRecordStringRepresentation() {
        Move move = new Move(new Position(6, 4), new Position(4, 4));
        Move equalMove = new Move(new Position(6, 4), new Position(4, 4));

        assertEquals(move, equalMove);
        assertEquals(move.hashCode(), equalMove.hashCode());
        assertNotEquals(move, new Move(new Position(6, 4), new Position(5, 4)));
        assertTrue(move.toString().startsWith("Move["));
    }

    @Test
    void storesPromotionAndSpecialMoveKinds() {
        Move promotion = new Move(
                new Position(1, 0), new Position(0, 0), null, Type.KNIGHT, MoveKind.PROMOTION);
        Move castle = new Move(
                new Position(7, 4), new Position(7, 6), null, null, MoveKind.CASTLING);
        Move enPassant = new Move(
                new Position(3, 4), new Position(2, 3), null, null, MoveKind.EN_PASSANT);

        assertEquals(Type.KNIGHT, promotion.promotionType());
        assertTrue(castle.isCastling());
        assertTrue(enPassant.isEnPassant());
    }

    @Test
    void rejectsInvalidPromotionMetadata() {
        assertThrows(IllegalArgumentException.class, () -> new Move(
                new Position(1, 0), new Position(0, 0), null, Type.KING, MoveKind.PROMOTION));
        assertThrows(IllegalArgumentException.class, () -> new Move(
                new Position(3, 4), new Position(2, 3), null, Type.QUEEN, MoveKind.EN_PASSANT));
        assertThrows(IllegalArgumentException.class, () -> new Move(
                new Position(1, 0), new Position(0, 0), null, null, MoveKind.PROMOTION));
    }

    @Test
    void capturedPieceMetadataIsPreservedWhenAddingCapture() {
        Position from = new Position(4, 0);
        Position to = new Position(4, 7);
        Move move = new Move(from, to);
        Piece captured = new Piece(Color.BLACK, Type.ROOK);

        Move capturedMove = move.withCapturedPiece(captured);

        assertEquals(from, capturedMove.from());
        assertEquals(to, capturedMove.to());
        assertSame(captured, capturedMove.capturedPiece());
        assertTrue(move.getCapturedPiece().isEmpty());
    }
}
