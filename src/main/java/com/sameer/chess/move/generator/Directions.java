package com.sameer.chess.move.generator;

import java.util.List;

final class Directions {

    static final List<Direction> DIAGONAL = List.of(
            new Direction(-1, -1), new Direction(-1, 1),
            new Direction(1, -1), new Direction(1, 1));

    static final List<Direction> ORTHOGONAL = List.of(
            new Direction(-1, 0), new Direction(1, 0),
            new Direction(0, -1), new Direction(0, 1));

    static final List<Direction> ALL = List.of(
            new Direction(-1, -1), new Direction(-1, 1),
            new Direction(1, -1), new Direction(1, 1),
            new Direction(-1, 0), new Direction(1, 0),
            new Direction(0, -1), new Direction(0, 1));

    private Directions() {
    }
}
