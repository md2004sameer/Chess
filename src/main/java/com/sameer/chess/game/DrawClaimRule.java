package com.sameer.chess.game;

/** Determines whether a draw may be claimed under a particular rule. */
@FunctionalInterface
public interface DrawClaimRule {
    boolean canClaim(GameState state, int currentPositionRepetitions);
}
