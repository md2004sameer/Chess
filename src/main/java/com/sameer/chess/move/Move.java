package com.sameer.chess.move;

import java.util.Objects;
import java.util.Optional;

import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Type;

public record Move(
        Position from,
        Position to,
        Piece capturedPiece,
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
        if ((kind == MoveKind.CASTLING || kind == MoveKind.EN_PASSANT) && promotionType != null) {
            throw new IllegalArgumentException("Special move cannot also be a promotion");
        }
    }

    public Move(Position from, Position to) {
        this(from, to, null, null, MoveKind.NORMAL);
    }

    public Move(Position from, Position to, Type promotionType, boolean castling, boolean enPassant) {
        this(from, to, null, promotionType, kindFromFlags(promotionType, castling, enPassant));
    }

    public Move(
            Position from,
            Position to,
            Piece capturedPiece,
            Type promotionType,
            boolean castling,
            boolean enPassant) {
        this(from, to, capturedPiece, promotionType, kindFromFlags(promotionType, castling, enPassant));
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Optional<Piece> getCapturedPiece() {
        return Optional.ofNullable(capturedPiece);
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

    public Move withCapturedPiece(Piece piece) {
        return new Move(from, to, Objects.requireNonNull(piece, "captured piece must not be null"),
                promotionType, kind);
    }

    private static MoveKind kindFromFlags(
            Type promotionType, boolean castling, boolean enPassant) {
        if (castling && enPassant) {
            throw new IllegalArgumentException("A move cannot be both castling and en passant");
        }
        if (castling) {
            return MoveKind.CASTLING;
        }
        if (enPassant) {
            return MoveKind.EN_PASSANT;
        }
        return promotionType == null ? MoveKind.NORMAL : MoveKind.PROMOTION;
    }
}
