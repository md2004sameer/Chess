package com.sameer.chess.engine;

import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.CastlingRights;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

/** Applies a previously validated move to its game state. */
public final class MoveExecutor {
    /** Callers must validate first; use Game.play(). */
    public void execute(GameState state, Move move) {
        applyUnchecked(state, move);
    }

    void applyUnchecked(GameState state, Move move) {
        Board board = state.getBoard();
        Piece moving = board.getPiece(move.from());
        if (moving == null) {
            throw new IllegalStateException("Cannot execute a move from an empty square.");
        }

        Position capturePosition = move.to();
        if (moving.type() == PieceType.PAWN && move.from().col() != move.to().col()
                && board.isEmpty(move.to()) && move.to().equals(state.getEnPassantTarget())) {
            capturePosition = new Position(move.from().row(), move.to().col());
        }
        Piece captured = board.getPiece(capturePosition);
        updateCastlingRights(state.getCastlingRights(), moving, move.from(), captured, capturePosition);

        boolean castling = moving.type() == PieceType.KING
                && move.from().row() == move.to().row()
                && Math.abs(move.from().col() - move.to().col()) == 2;

        board.setPiece(move.from(), null);
        if (!capturePosition.equals(move.to())) board.setPiece(capturePosition, null);
        Piece placed = move.promotionType() == null
                ? moving : new Piece(moving.color(), move.promotionType());
        board.setPiece(move.to(), placed);

        if (castling) {
            int row = move.from().row();
            boolean kingSide = move.to().col() == 6;
            Position rookFrom = new Position(row, kingSide ? 7 : 0);
            Position rookTo = new Position(row, kingSide ? 5 : 3);
            Piece rook = board.getPiece(rookFrom);
            board.setPiece(rookFrom, null);
            board.setPiece(rookTo, rook);
        }

        Position enPassantTarget = null;
        if (moving.type() == PieceType.PAWN
                && Math.abs(move.to().row() - move.from().row()) == 2) {
            enPassantTarget = new Position(
                    (move.from().row() + move.to().row()) / 2, move.from().col());
        }
        state.setEnPassantTarget(enPassantTarget);
        if (moving.type() == PieceType.PAWN || captured != null) state.setHalfmoveClock(0);
        else state.incrementHalfmoveClock();
        if (moving.color() == Color.BLACK) state.incrementFullmoveNumber();
        state.setActiveColor(state.getActiveColor().opposite());
        state.setStatus(GameStatus.ACTIVE);
    }

    private void updateCastlingRights(CastlingRights rights, Piece moving,
                                      Position from, Piece captured, Position capturePosition) {
        if (moving.type() == PieceType.KING) rights.disableBoth(moving.color());
        if (moving.type() == PieceType.ROOK) clearRookRight(rights, moving.color(), from);
        if (captured != null && captured.type() == PieceType.ROOK) {
            clearRookRight(rights, captured.color(), capturePosition);
        }
    }

    private void clearRookRight(CastlingRights rights, Color color, Position rookSquare) {
        if (rookSquare.row() != color.homeRow()) return;
        if (rookSquare.col() == 0) rights.disableQueenSide(color);
        if (rookSquare.col() == 7) rights.disableKingSide(color);
    }
}
