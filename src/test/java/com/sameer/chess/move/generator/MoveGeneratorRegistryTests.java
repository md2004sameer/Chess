package com.sameer.chess.move.generator;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sameer.chess.model.enums.Type;

class MoveGeneratorRegistryTests {

    @Test
    void configurationRegistersAGeneratorForEveryPieceType() {
        MoveGeneratorRegistry registry =
                new MoveGeneratorConfiguration().moveGeneratorRegistry();

        assertInstanceOf(KingMoveGenerator.class, registry.getGenerator(Type.KING));
        assertInstanceOf(QueenMoveGenerator.class, registry.getGenerator(Type.QUEEN));
        assertInstanceOf(RookMoveGenerator.class, registry.getGenerator(Type.ROOK));
        assertInstanceOf(BishopMoveGenerator.class, registry.getGenerator(Type.BISHOP));
        assertInstanceOf(KnightMoveGenerator.class, registry.getGenerator(Type.KNIGHT));
        assertInstanceOf(PawnMoveGenerator.class, registry.getGenerator(Type.PAWN));
    }

    @Test
    void rejectsNullRegistryAndNullPieceType() {
        assertThrows(NullPointerException.class, () -> new MoveGeneratorRegistry(null));
        MoveGeneratorRegistry registry =
                new MoveGeneratorConfiguration().moveGeneratorRegistry();
        assertThrows(NullPointerException.class, () -> registry.getGenerator(null));
        Map<Type, MoveGenerator> nullEntry = new HashMap<>();
        nullEntry.put(Type.ROOK, null);
        assertThrows(NullPointerException.class, () -> new MoveGeneratorRegistry(nullEntry));
        Map<Type, MoveGenerator> nullKey = new HashMap<>();
        nullKey.put(null, new RookMoveGenerator());
        assertThrows(NullPointerException.class, () -> new MoveGeneratorRegistry(nullKey));
        assertThrows(IllegalArgumentException.class, () ->
                new MoveGeneratorRegistry(Map.of(Type.PAWN, new PawnMoveGenerator()))
                        .getGenerator(Type.ROOK));
    }
}
