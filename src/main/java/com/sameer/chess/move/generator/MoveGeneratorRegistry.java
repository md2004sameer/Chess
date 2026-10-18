package com.sameer.chess.move.generator;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import com.sameer.chess.model.enums.Type;

public class MoveGeneratorRegistry {

    private final Map<Type, MoveGenerator> generators;

    public MoveGeneratorRegistry(Map<Type, MoveGenerator> generators) {
        Objects.requireNonNull(generators, "generators must not be null");
        EnumMap<Type, MoveGenerator> registered = new EnumMap<>(Type.class);
        generators.forEach((type, generator) ->
                registered.put(
                        Objects.requireNonNull(type, "piece type must not be null"),
                        Objects.requireNonNull(generator, "move generator must not be null")));
        this.generators = Map.copyOf(registered);
    }

    public MoveGenerator getGenerator(Type type) {
        Objects.requireNonNull(type, "piece type must not be null");
        MoveGenerator generator = generators.get(type);
        if (generator == null) {
            throw new IllegalArgumentException("No move generator registered for " + type);
        }
        return generator;
    }
}
