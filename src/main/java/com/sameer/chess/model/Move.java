package com.sameer.chess.model;

import java.util.Objects;

/** Describes a move between two squares and an optional pawn promotion. */
public record Move(Position from, Position to, PieceType promotionType) {
    public Move {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
    }

    public static Move parse(String input) {
        if (input == null || !input.matches("[a-h][1-8][a-h][1-8][qrbn]?")) {
            throw new IllegalArgumentException(
                    "Enter a move like e2e4 or a promotion like e7e8q.");
        }
        Position from = Position.parse(input.substring(0, 2));
        Position to = Position.parse(input.substring(2, 4));
        PieceType promotion = input.length() == 5
                ? PieceType.fromPromotionChar(input.charAt(4))
                : null;
        return new Move(from, to, promotion);
    }

    @Override
    public String toString() {
        return from + "" + to
                + (promotionType == null ? "" : Character.toLowerCase(promotionType.symbol()));
    }
}
