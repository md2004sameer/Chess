package com.sameer.chess.model;

/** Tracks which king- and queen-side castling options remain available. */
public record CastlingRights(
        boolean whiteKingSide,
        boolean whiteQueenSide,
        boolean blackKingSide,
        boolean blackQueenSide) {

    public boolean canCastle(Color color, boolean kingSide) {
        if (color == Color.WHITE) {
            return kingSide ? whiteKingSide : whiteQueenSide;
        }
        return kingSide ? blackKingSide : blackQueenSide;
    }

    public CastlingRights withoutKingSide(Color color) {
        return color == Color.WHITE
                ? new CastlingRights(false, whiteQueenSide, blackKingSide, blackQueenSide)
                : new CastlingRights(whiteKingSide, whiteQueenSide, false, blackQueenSide);
    }

    public CastlingRights withoutQueenSide(Color color) {
        return color == Color.WHITE
                ? new CastlingRights(whiteKingSide, false, blackKingSide, blackQueenSide)
                : new CastlingRights(whiteKingSide, whiteQueenSide, blackKingSide, false);
    }

    public CastlingRights withoutBoth(Color color) {
        return withoutKingSide(color).withoutQueenSide(color);
    }
}
