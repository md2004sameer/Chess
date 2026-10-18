package com.sameer.chess.move.generator;

import com.sameer.chess.model.enums.Type;

public class RookMoveGenerator extends SlidingMoveGenerator {

    private static final int[][] DIRECTIONS = {
        {-1, 0}, {1, 0},
        {0, -1}, {0, 1}
    };

    @Override
    public Type type() {
        return Type.ROOK;
    }

    @Override
    protected int[][] directions() {
        return DIRECTIONS;
    }
}
