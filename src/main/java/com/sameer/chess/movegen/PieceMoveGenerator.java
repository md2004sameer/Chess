package com.sameer.chess.movegen;

import java.util.List;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Position;

/** Generates pseudo-legal moves for one piece type from a given position. */
public interface PieceMoveGenerator {
    List<Move> generate(GameState state, Position from);
}
