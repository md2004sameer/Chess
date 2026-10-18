package com.sameer.chess.model;

import java.util.Objects;

import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;

public class Piece {

    private final Color color;
    private final Type type;

    public Piece(Color color, Type type) {
        this.color = Objects.requireNonNull(color, "color must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public Color getColor() {
        return color;
    }

    public Type getType() {
        return type;
    }
}
