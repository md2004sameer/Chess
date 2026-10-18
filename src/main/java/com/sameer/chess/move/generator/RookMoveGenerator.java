package com.sameer.chess.move.generator;

import java.util.List;

import com.sameer.chess.model.enums.Type;

public class RookMoveGenerator extends SlidingMoveGenerator {

    @Override
    public Type type() {
        return Type.ROOK;
    }

    @Override
    protected List<Direction> directions() {
        return Directions.ORTHOGONAL;
    }
}
