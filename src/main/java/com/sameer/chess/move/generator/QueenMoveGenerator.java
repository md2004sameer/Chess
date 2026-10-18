package com.sameer.chess.move.generator;

import java.util.List;

import com.sameer.chess.model.enums.Type;

public class QueenMoveGenerator extends SlidingMoveGenerator {

    @Override
    public Type type() {
        return Type.QUEEN;
    }

    @Override
    protected List<Direction> directions() {
        return Directions.ALL;
    }
}
