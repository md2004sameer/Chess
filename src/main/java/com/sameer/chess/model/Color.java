package com.sameer.chess.model;

/** Identifies a side and provides color-specific movement directions. */
public enum Color {
    WHITE, BLACK;

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    public int pawnDirection() {
        return this == WHITE ? -1 : 1;
    }

    public int homeRow() {
        return this == WHITE ? 7 : 0;
    }

    public int pawnStartRow() {
        return this == WHITE ? 6 : 1;
    }

    public int promotionRow() {
        return this == WHITE ? 0 : 7;
    }
}
