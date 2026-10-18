package com.sameer.chess.move.generator;

import com.sameer.chess.model.enums.Type;

public class KnightMoveGenerator extends StepMoveGenerator {

    private static final int[][] OFFSETS = {
        {-2, -1}, {-2, 1},
        {-1, -2}, {-1, 2},
        {1, -2}, {1, 2},
        {2, -1}, {2, 1}
    };

    @Override
    public Type type() {
        return Type.KNIGHT;
    }

    @Override
    protected int[][] offsets() {
        return OFFSETS;
    }
}
