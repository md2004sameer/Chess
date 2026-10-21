package com.sameer.chess.model;

/** Represents a board square using zero-based row and column coordinates. */
public record Position(int row, int col) {
    public boolean isInsideBoard() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public static Position parse(String square) {
        if (square == null || !square.matches("[a-h][1-8]")) {
            throw new IllegalArgumentException("Square must look like e2.");
        }
        int col = square.charAt(0) - 'a';
        int row = 8 - Character.digit(square.charAt(1), 10);
        return new Position(row, col);
    }

    @Override
    public String toString() {
        if (!isInsideBoard()) {
            return "(" + row + "," + col + ")";
        }
        return "" + (char) ('a' + col) + (char) ('8' - row);
    }
}
