package com.Chess.move;

import com.Chess.model.Position;

public class Move {

    private Position from;
    private Position to ;

    public Move(Position from , Position to ){
        this.from = from;
        this.to = to;
    }

    public Position getFrom(){
        return from;

    }

    public Position getTo(){
        return to;
    }
    
    
}
