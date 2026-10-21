package com.sameer.chess.movegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import com.sameer.chess.model.PieceType;

class MoveGeneratorFactoryTests {
    @Test
    void returnsExpectedGeneratorClassForEveryPieceType() {
        assertEquals(KingMoveGenerator.class,
                MoveGeneratorFactory.forPiece(PieceType.KING).getClass());
        assertEquals(QueenMoveGenerator.class,
                MoveGeneratorFactory.forPiece(PieceType.QUEEN).getClass());
        assertEquals(RookMoveGenerator.class,
                MoveGeneratorFactory.forPiece(PieceType.ROOK).getClass());
        assertEquals(BishopMoveGenerator.class,
                MoveGeneratorFactory.forPiece(PieceType.BISHOP).getClass());
        assertEquals(KnightMoveGenerator.class,
                MoveGeneratorFactory.forPiece(PieceType.KNIGHT).getClass());
        assertEquals(PawnMoveGenerator.class,
                MoveGeneratorFactory.forPiece(PieceType.PAWN).getClass());
    }

    @Test
    void reusesStatelessGeneratorInstances() {
        for (PieceType type : PieceType.values()) {
            assertSame(MoveGeneratorFactory.forPiece(type), MoveGeneratorFactory.forPiece(type));
        }
    }
}
