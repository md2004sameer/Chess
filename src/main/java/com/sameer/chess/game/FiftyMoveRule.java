package com.sameer.chess.game;

/** Implements the fifty-move draw claim threshold. */
public final class FiftyMoveRule implements DrawClaimRule {
    @Override
    public boolean canClaim(GameState state, int repetitions) {
        return state.getHalfmoveClock() >= 100;
    }
}
