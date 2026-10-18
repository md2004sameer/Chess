package com.sameer.chess.move.generator;

import com.sameer.chess.model.enums.Type;

public class BishopMoveGenerator extends SlidingMoveGenerator {

    private static final int[][] DIRECTIONS = {
        {-1, -1}, {-1, 1},
        {1, -1}, {1, 1}
    };

    @Override
    public Type type() {
        return Type.BISHOP;
    }

    @Override
    public int[][] directions() {
        return DIRECTIONS;
    }
}
