package com.sameer.chess.engine;

import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.ValidationResult;

/** Validates one independent rule for a proposed chess move. */
@FunctionalInterface
public interface MoveRule {
    ValidationResult validate(GameState state, Move move);
}
