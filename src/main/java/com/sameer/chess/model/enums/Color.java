package com.sameer.chess.model.enums;

public enum Color {
    WHITE(7, 6, 0, -1),
    BLACK(0, 1, 7, 1);

    private final int homeRow;
    private final int pawnRow;
    private final int promotionRow;
    private final int forward;

    Color(int homeRow, int pawnRow, int promotionRow, int forward) {
        this.homeRow = homeRow;
        this.pawnRow = pawnRow;
        this.promotionRow = promotionRow;
        this.forward = forward;
    }

    public int homeRow() {
        return homeRow;
    }

    public int pawnRow() {
        return pawnRow;
    }

    public int promotionRow() {
        return promotionRow;
    }

    public int forward() {
        return forward;
    }

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
