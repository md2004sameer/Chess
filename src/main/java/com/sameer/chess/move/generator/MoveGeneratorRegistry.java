package com.sameer.chess.move.generator;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.List;

import com.sameer.chess.model.enums.Type;

public class MoveGeneratorRegistry {

    private final Map<Type, MoveGenerator> generators;

    public MoveGeneratorRegistry(List<MoveGenerator> generators) {
        Objects.requireNonNull(generators, "generators must not be null");
        EnumMap<Type, MoveGenerator> registered = new EnumMap<>(Type.class);
        for (MoveGenerator generator : generators) {
            Objects.requireNonNull(generator, "move generator must not be null");
            Type type = Objects.requireNonNull(generator.type(), "piece type must not be null");
            if (registered.putIfAbsent(type, generator) != null) {
                throw new IllegalArgumentException("Duplicate move generator for " + type);
            }
        }
        this.generators = Collections.unmodifiableMap(registered);
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
