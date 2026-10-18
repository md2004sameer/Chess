package com.sameer.chess.move.generator;

import com.sameer.chess.model.enums.Type;

public class QueenMoveGenerator extends SlidingMoveGenerator {

    private static final int[][] DIRECTIONS = {
        {-1, -1}, {-1, 1},
        {1, -1}, {1, 1},
        {-1, 0}, {1, 0},
        {0, -1}, {0, 1}
    };

    @Override
    public Type type() {
        return Type.QUEEN;
    }

    @Override
    protected int[][] directions() {
        return DIRECTIONS;
    }
}
