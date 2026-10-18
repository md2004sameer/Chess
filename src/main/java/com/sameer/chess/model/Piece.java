package com.sameer.chess.model;

import java.util.Objects;

import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;

public record Piece(Color color, Type type) {

    public Piece {
        Objects.requireNonNull(color, "color must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }

    public Color getColor() {
        return color;
    }

    public Type getType() {
        return type;
    }
}
