package com.sameer.chess.game;

import java.util.Objects;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.CastlingRights;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;

/** Holds a position and the metadata required to continue a chess game. */
public final class GameState {
    private final Board board;
    private Color activeColor;
    private CastlingRights castlingRights;
    private Position enPassantTarget;
    private int halfmoveClock;
    private int fullmoveNumber;
    private GameStatus status;

    public GameState(Board board, Color activeColor, CastlingRights castlingRights,
                     Position enPassantTarget, int halfmoveClock, int fullmoveNumber,
                     GameStatus status) {
        this.board = Objects.requireNonNull(board, "board");
        this.activeColor = Objects.requireNonNull(activeColor, "activeColor");
        this.castlingRights = Objects.requireNonNull(castlingRights, "castlingRights");
        this.enPassantTarget = enPassantTarget;
        this.halfmoveClock = halfmoveClock;
        this.fullmoveNumber = fullmoveNumber;
        this.status = Objects.requireNonNull(status, "status");
    }

    public static GameState initial() {
        return new GameState(
                Board.initialPosition(), Color.WHITE,
                new CastlingRights(true, true, true, true),
                null, 0, 1, GameStatus.ACTIVE);
    }

    public Board getBoard() { return board; }
    public Color getActiveColor() { return activeColor; }
    public CastlingRights getCastlingRights() { return castlingRights; }
    public Position getEnPassantTarget() { return enPassantTarget; }
    public int getHalfmoveClock() { return halfmoveClock; }
    public int getFullmoveNumber() { return fullmoveNumber; }
    public GameStatus getStatus() { return status; }

    /** Updates status internally; engine-internal, do not call from outside the engine. */
    public void setStatus(GameStatus status) { this.status = status; }
    /** Updates the active side during execution; engine-internal, do not call from outside the engine. */
    public void setActiveColor(Color color) { this.activeColor = color; }
    /** Updates castling metadata during execution; engine-internal, do not call from outside the engine. */
    public void setCastlingRights(CastlingRights rights) { this.castlingRights = rights; }
    /** Updates en passant metadata during execution; engine-internal, do not call from outside the engine. */
    public void setEnPassantTarget(Position target) { this.enPassantTarget = target; }
    /** Updates the halfmove clock during execution; engine-internal, do not call from outside the engine. */
    public void setHalfmoveClock(int clock) { this.halfmoveClock = clock; }
    /** Advances the halfmove clock; engine-internal, do not call from outside the engine. */
    public void incrementHalfmoveClock() { halfmoveClock++; }
    /** Advances the fullmove number; engine-internal, do not call from outside the engine. */
    public void incrementFullmoveNumber() { fullmoveNumber++; }

    /** Copies state for safe move simulations without mutating the current position. */
    public GameState copy() {
        return new GameState(board.copy(), activeColor, castlingRights.copy(),
                enPassantTarget, halfmoveClock, fullmoveNumber, status);
    }

    String positionKey(boolean includeEnPassantTarget) {
        StringBuilder key = new StringBuilder(80);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                key.append(piece == null ? '.' : piece.displaySymbol());
            }
        }
        key.append('|').append(activeColor)
                .append('|').append(castlingRights.key())
                .append('|').append(!includeEnPassantTarget || enPassantTarget == null
                        ? "-" : enPassantTarget);
        return key.toString();
    }
}
