package com.sameer.chess.move;

import java.util.Objects;
import java.util.Optional;

import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Type;

public record Move(
        Position from,
        Position to,
        Type promotionType,
        MoveKind kind) {

    public Move {
        Objects.requireNonNull(from, "from position must not be null");
        Objects.requireNonNull(to, "to position must not be null");
        Objects.requireNonNull(kind, "move kind must not be null");
        if (promotionType != null
                && promotionType != Type.QUEEN
                && promotionType != Type.ROOK
                && promotionType != Type.BISHOP
                && promotionType != Type.KNIGHT) {
            throw new IllegalArgumentException("A pawn can only promote to queen, rook, bishop, or knight");
        }
        if ((kind == MoveKind.PROMOTION) != (promotionType != null)) {
            throw new IllegalArgumentException("Promotion moves must include exactly one promotion type");
        }
    }

    public Move(Position from, Position to) {
        this(from, to, null, MoveKind.NORMAL);
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Optional<Type> getPromotionType() {
        return Optional.ofNullable(promotionType);
    }

    public boolean isCastling() {
        return kind == MoveKind.CASTLING;
    }

    public boolean isEnPassant() {
        return kind == MoveKind.EN_PASSANT;
    }

    public Position capturePosition() {
        return kind == MoveKind.EN_PASSANT ? new Position(from.row(), to.col()) : to;
    }
}
