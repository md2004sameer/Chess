package com.sameer.chess.model;

/** Describes the game's current position, including terminal draw conditions. */
public enum GameStatus {
    ACTIVE,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_FIFTY_MOVE,
    DRAW_THREEFOLD_REPETITION,
    DRAW_INSUFFICIENT_MATERIAL,
    DRAW_SEVENTY_FIVE_MOVE,
    DRAW_FIVEFOLD_REPETITION;

    public boolean isTerminal() {
        return this != ACTIVE && this != CHECK;
    }
}
