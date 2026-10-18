package com.sameer.chess.model;

public record Position(int row, int col) {

    public Position {
        if (!isOnBoard(row, col)) {
            throw new IllegalArgumentException("Position must be within the board");
        }
    }

    public static boolean isOnBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
