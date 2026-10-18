package com.sameer.chess.move.generator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.sameer.chess.model.enums.Type;

class MoveGeneratorRegistryTests {

    @Test
    void configurationRegistersAGeneratorForEveryPieceType() {
        MoveGeneratorRegistry registry =
                new MoveGeneratorConfiguration().moveGeneratorRegistry();

        for (Type type : Type.values()) {
            assertNotNull(registry.getGenerator(type));
        }
    }

    @Test
    void rejectsNullRegistryAndNullPieceType() {
        assertThrows(NullPointerException.class, () -> new MoveGeneratorRegistry(null));
        MoveGeneratorRegistry registry =
                new MoveGeneratorConfiguration().moveGeneratorRegistry();
        assertThrows(NullPointerException.class, () -> registry.getGenerator(null));
    }
}
