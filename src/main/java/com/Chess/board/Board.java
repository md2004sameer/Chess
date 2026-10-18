package com.Chess.board;

import java.util.Objects;

import com.Chess.model.Piece;
import com.Chess.model.Position;

public class Board {

    private final Piece[][] board = new Piece[8][8];


    public Piece getPiece(Position position){
        return board[position.getRow()][position.getCol()];
    }

    public void placePiece(Piece piece) {
        Objects.requireNonNull(piece, "piece must not be null");
        Position position = piece.getPosition();
        if (getPiece(position) != null) {
            throw new IllegalArgumentException("Square is already occupied");
        }
        board[position.getRow()][position.getCol()] = piece;
    }
}
