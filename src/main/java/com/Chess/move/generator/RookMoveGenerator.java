package com.Chess.move.generator;

public class RookMoveGenerator extends SlidingMoveGenerator {

    private static final int[][] DIRECTIONS = {
        {-1, 0}, {1, 0},
        {0, -1}, {0, 1}
    };

    @Override
    protected int[][] directions() {
        return DIRECTIONS;
    }
}
