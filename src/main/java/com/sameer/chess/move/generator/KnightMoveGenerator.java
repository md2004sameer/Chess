package com.sameer.chess.move.generator;

import java.util.List;

import com.sameer.chess.model.enums.Type;

public class KnightMoveGenerator extends StepMoveGenerator {

    private static final List<Direction> DIRECTIONS = List.of(
            new Direction(-2, -1), new Direction(-2, 1),
            new Direction(-1, -2), new Direction(-1, 2),
            new Direction(1, -2), new Direction(1, 2),
            new Direction(2, -1), new Direction(2, 1));

    @Override
    public Type type() {
        return Type.KNIGHT;
    }

    @Override
    protected List<Direction> directions() {
        return DIRECTIONS;
    }
}
