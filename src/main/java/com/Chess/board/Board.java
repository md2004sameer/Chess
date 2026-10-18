package com.Chess.board;

import com.Chess.model.Piece;
import com.Chess.model.Position;

public class Board {

    private final Piece[][] board = new Piece[8][8];


    Piece getPiece(Position position){
        return board[position.getRow()][position.getCol()];
    }
    
}
