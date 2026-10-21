package com.sameer.chess.movegen;

import java.util.List;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Position;

/** Generates queen moves independently from other piece movement rules. */
public final class QueenMoveGenerator implements PieceMoveGenerator {
    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    @Override
    public List<Move> generate(GameState state, Position from) {
        return SlidingMoves.generate(state, from, DIRECTIONS);
    }
}
