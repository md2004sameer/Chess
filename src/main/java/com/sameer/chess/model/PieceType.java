package com.sameer.chess.model;

/** Identifies a chess piece kind and its display symbol. */
public enum PieceType {
    KING('K'), QUEEN('Q'), ROOK('R'), BISHOP('B'), KNIGHT('N'), PAWN('P');

    private final char symbol;

    PieceType(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }

    public static PieceType fromPromotionChar(char value) {
        return switch (Character.toLowerCase(value)) {
            case 'q' -> QUEEN;
            case 'r' -> ROOK;
            case 'b' -> BISHOP;
            case 'n' -> KNIGHT;
            default -> throw new IllegalArgumentException("Promotion must be q, r, b, or n.");
        };
    }
}
