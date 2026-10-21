package com.sameer.chess.model;

/** Tracks which king- and queen-side castling options remain available. */
public final class CastlingRights {
    private boolean whiteKingSide;
    private boolean whiteQueenSide;
    private boolean blackKingSide;
    private boolean blackQueenSide;

    public CastlingRights(boolean whiteKingSide, boolean whiteQueenSide,
                          boolean blackKingSide, boolean blackQueenSide) {
        this.whiteKingSide = whiteKingSide;
        this.whiteQueenSide = whiteQueenSide;
        this.blackKingSide = blackKingSide;
        this.blackQueenSide = blackQueenSide;
    }

    public boolean canCastle(Color color, boolean kingSide) {
        if (color == Color.WHITE) {
            return kingSide ? whiteKingSide : whiteQueenSide;
        }
        return kingSide ? blackKingSide : blackQueenSide;
    }

    public void disableKingSide(Color color) {
        if (color == Color.WHITE) whiteKingSide = false;
        else blackKingSide = false;
    }

    public void disableQueenSide(Color color) {
        if (color == Color.WHITE) whiteQueenSide = false;
        else blackQueenSide = false;
    }

    public void disableBoth(Color color) {
        disableKingSide(color);
        disableQueenSide(color);
    }

    public CastlingRights copy() {
        return new CastlingRights(
                whiteKingSide, whiteQueenSide, blackKingSide, blackQueenSide);
    }

    public String key() {
        return "" + (whiteKingSide ? 'K' : '-') + (whiteQueenSide ? 'Q' : '-')
                + (blackKingSide ? 'k' : '-') + (blackQueenSide ? 'q' : '-');
    }
}
