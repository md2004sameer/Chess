package com.sameer.chess.move.generator;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sameer.chess.model.enums.Type;

class MoveGeneratorRegistryTests {

    @Test
    void configurationRegistersAGeneratorForEveryPieceType() {
        MoveGeneratorRegistry registry =
                new MoveGeneratorConfiguration().moveGeneratorRegistry();

        for (Type type : Type.values()) {
            assertEquals(type, registry.getGenerator(type).type());
        }
        assertInstanceOf(KingMoveGenerator.class, registry.getGenerator(Type.KING));
        assertInstanceOf(QueenMoveGenerator.class, registry.getGenerator(Type.QUEEN));
        assertInstanceOf(RookMoveGenerator.class, registry.getGenerator(Type.ROOK));
        assertInstanceOf(BishopMoveGenerator.class, registry.getGenerator(Type.BISHOP));
        assertInstanceOf(KnightMoveGenerator.class, registry.getGenerator(Type.KNIGHT));
        assertInstanceOf(PawnMoveGenerator.class, registry.getGenerator(Type.PAWN));
    }

    @Test
    void rejectsDuplicateTypes() {
        assertThrows(IllegalArgumentException.class,
                () -> new MoveGeneratorRegistry(List.of(new KingMoveGenerator(), new KingMoveGenerator())));
    }

    @Test
    void rejectsNullRegistryElementsAndPieceType() {
        assertThrows(NullPointerException.class, () -> new MoveGeneratorRegistry(null));
        MoveGeneratorRegistry registry =
                new MoveGeneratorConfiguration().moveGeneratorRegistry();
        assertThrows(NullPointerException.class, () -> registry.getGenerator(null));
        assertThrows(NullPointerException.class,
                () -> new MoveGeneratorRegistry(Collections.singletonList(null)));
        assertThrows(IllegalArgumentException.class, () ->
                new MoveGeneratorRegistry(List.of(new PawnMoveGenerator()))
                        .getGenerator(Type.ROOK));
    }
}
