package com.Chess.model;

import java.util.Objects;

import com.Chess.model.enums.Color;
import com.Chess.model.enums.Type;

public class Piece {

    private final Color color;
    private final Type type;
    private Position position;

    public Piece(Color color, Type type, Position position) {
        this.color = Objects.requireNonNull(color, "color must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    public Color getColor() {
        return color;
    }

    public Type getType() {
        return type;
    }

    public Position getPosition() {
        return position;
    }

    
}
