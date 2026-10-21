package com.sameer.chess.engine;

import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.ValidationResult;

/** Checks castling rights, clear paths, and attacked king-path squares. */
final class CastlingChecker {
    private final AttackDetector attackDetector;

    CastlingChecker(AttackDetector attackDetector) {
        this.attackDetector = attackDetector;
    }

    ValidationResult validate(GameState state, Move move) {
        Piece king = pieceAt(state, move == null ? null : move.from());
        if (king == null || move == null || king.type() != PieceType.KING
                || Math.abs(move.to().col() - move.from().col()) != 2) {
            return ValidationResult.ok();
        }
        boolean kingSide = move.to().col() == 6;
        if (!hasValidCastlingSquares(king, move)) {
            return ValidationResult.reject("The king is not on its castling square.");
        }
        if (!state.getCastlingRights().canCastle(king.color(), kingSide)) {
            return ValidationResult.reject("Castling rights are not available.");
        }
        if (!hasRequiredRook(state, king, kingSide)) {
            return ValidationResult.reject("The rook required for castling is not present.");
        }
        if (!isPathClear(state, king.color().homeRow(), kingSide)) {
            return ValidationResult.reject("Squares between the king and rook must be empty.");
        }
        if (isKingPathAttacked(state, king.color(), kingSide)) {
            return ValidationResult.reject("The king cannot castle through or out of check.");
        }
        return ValidationResult.ok();
    }

    private boolean hasValidCastlingSquares(Piece king, Move move) {
        int homeRow = king.color().homeRow();
        return move.from().row() == homeRow && move.from().col() == 4
                && move.to().row() == homeRow
                && (move.to().col() == 2 || move.to().col() == 6);
    }

    private boolean hasRequiredRook(GameState state, Piece king, boolean kingSide) {
        int row = king.color().homeRow();
        int col = kingSide ? 7 : 0;
        Piece rook = state.getBoard().getPiece(new Position(row, col));
        return rook != null && rook.type() == PieceType.ROOK && rook.color() == king.color();
    }

    private boolean isPathClear(GameState state, int row, boolean kingSide) {
        int firstCol = kingSide ? 5 : 1;
        int lastCol = kingSide ? 6 : 3;
        for (int col = firstCol; col <= lastCol; col++) {
            if (!state.getBoard().isEmpty(new Position(row, col))) return false;
        }
        return true;
    }

    private boolean isKingPathAttacked(GameState state, Color color, boolean kingSide) {
        Board board = state.getBoard().copy();
        int row = color.homeRow();
        board.setPiece(new Position(row, 4), null);
        int step = kingSide ? 1 : -1;
        int finalCol = kingSide ? 6 : 2;
        for (int col = 4; col != finalCol + step; col += step) {
            if (attackDetector.isSquareAttacked(
                    board, new Position(row, col), color.opposite())) return true;
        }
        return false;
    }

    private Piece pieceAt(GameState state, Position position) {
        if (state == null || !state.getBoard().isInside(position)) return null;
        return state.getBoard().getPiece(position);
    }
}
