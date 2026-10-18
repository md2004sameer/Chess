package com.sameer.chess.move.generator;

import com.sameer.chess.model.enums.Type;

public class KingMoveGenerator extends StepMoveGenerator {

    private static final int[][] OFFSETS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1},             {0, 1},
        {1, -1},  {1, 0},  {1, 1}
    };

    @Override
    public Type type() {
        return Type.KING;
    }

    @Override
    protected int[][] offsets() {
        return OFFSETS;
    }
}
