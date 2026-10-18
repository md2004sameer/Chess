package com.sameer.chess.move.generator;

import java.util.List;

import com.sameer.chess.model.enums.Type;

public class BishopMoveGenerator extends SlidingMoveGenerator {

    @Override
    public Type type() {
        return Type.BISHOP;
    }

    @Override
    protected List<Direction> directions() {
        return Directions.DIAGONAL;
    }
}
