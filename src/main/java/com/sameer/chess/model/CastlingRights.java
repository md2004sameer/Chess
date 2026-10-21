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

    /** Clears one side's king-side right; engine-internal, do not call from outside the engine. */
    public void disableKingSide(Color color) {
        if (color == Color.WHITE) whiteKingSide = false;
        else blackKingSide = false;
    }

    /** Clears one side's queen-side right; engine-internal, do not call from outside the engine. */
    public void disableQueenSide(Color color) {
        if (color == Color.WHITE) whiteQueenSide = false;
        else blackQueenSide = false;
    }

    /** Clears both rights for one side; engine-internal, do not call from outside the engine. */
    public void disableBoth(Color color) {
        disableKingSide(color);
        disableQueenSide(color);
    }

    /** Copies castling rights for an independent game-state simulation. */
    public CastlingRights copy() {
        return new CastlingRights(
                whiteKingSide, whiteQueenSide, blackKingSide, blackQueenSide);
    }

    /** Returns a compact key for position repetition tracking. */
    public String key() {
        return "" + (whiteKingSide ? 'K' : '-') + (whiteQueenSide ? 'Q' : '-')
                + (blackKingSide ? 'k' : '-') + (blackQueenSide ? 'q' : '-');
    }
}
