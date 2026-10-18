package com.sameer.chess.move.generator;

public class BishopMoveGenerator extends SlidingMoveGenerator {

    private static final int[][] DIRECTIONS = {
        {-1, -1}, {-1, 1},
        {1, -1}, {1, 1}
    };

    @Override
    public int[][] directions() {
        return DIRECTIONS;
    }
}
