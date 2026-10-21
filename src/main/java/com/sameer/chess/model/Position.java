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
        return new Position(8 - (square.charAt(1) - '0'), square.charAt(0) - 'a');
    }

    @Override
    public String toString() {
        if (!isInsideBoard()) {
            return "(" + row + "," + col + ")";
        }
        return "" + (char) ('a' + col) + (char) ('8' - row);
    }
}
