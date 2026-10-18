package com.Chess.model;

import com.Chess.model.enums.Color;
import com.Chess.model.enums.Type;

public class Piece {

    private final Color color;
    private final Type type;
    private Position position;

    Piece(Color color , Type type , Position position){
        this.color = color;
        this.type = type;
        this.position = position;
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
