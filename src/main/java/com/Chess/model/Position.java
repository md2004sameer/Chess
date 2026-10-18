package com.Chess.model;

public class Position {

    private int row;
    private int col;

    Position(int row , int col){
         if (row < 0 || row >= 8 || col < 0 || col >= 8) {
            throw new IllegalArgumentException("Position must be within the board");
        }
        this.row = row;
        this.col = col;
    }

    public int getRow(){
        return row;

    }
    public int getCol(){
        return col;
    }
    
}
