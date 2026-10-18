package com.sameer.chess.move.generator;

import java.util.List;

import com.sameer.chess.model.enums.Type;

public class KingMoveGenerator extends StepMoveGenerator {

    @Override
    public Type type() {
        return Type.KING;
    }

    @Override
    protected List<Direction> directions() {
        return Directions.ALL;
    }
}
