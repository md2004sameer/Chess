package com.sameer.chess.game;

/** Implements the threefold-repetition draw claim threshold. */
public final class ThreefoldRepetitionRule implements DrawClaimRule {
    @Override
    public boolean canClaim(GameState state, int repetitions) {
        return repetitions >= 3;
    }
}
