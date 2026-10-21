package com.sameer.chess.model;

/** Describes whether play continues, is in check, or has ended. */
public enum GameStatus {
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE
}
