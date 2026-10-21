package com.sameer.chess.model;

import java.util.Objects;

/** Describes a move between two squares and an optional pawn promotion. */
public record Move(Position from, Position to, PieceType promotion) {
    public Move {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
    }
}
