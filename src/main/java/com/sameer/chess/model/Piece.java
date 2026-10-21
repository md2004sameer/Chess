package com.sameer.chess.model;

import java.util.Objects;

/** Represents a chess piece by its side and kind. */
public record Piece(Color color, PieceType type) {
    public Piece {
        Objects.requireNonNull(color, "color");
        Objects.requireNonNull(type, "type");
    }
}
