package com.sameer.chess.model;

/** Identifies a side and provides the opposing side for turn changes. */
public enum Color {
    WHITE,
    BLACK;

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
