package com.sameer.chess.engine;

import java.util.List;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.ValidationResult;
import com.sameer.chess.movegen.MoveGenerator;

/** Builds the ordered validation rules used by the chess move validator. */
final class StandardMoveRules {
    private StandardMoveRules() {
    }

    static List<MoveRule> create(MoveGenerator generator, MoveExecutor executor,
                                 AttackDetector attackDetector) {
        return List.of(
                (state, move) -> checkCoordinates(move),
                (state, move) -> checkPieceAndTurn(state, move),
                (state, move) -> checkPromotion(state, move),
                (state, move) -> checkMovementPattern(state, move, generator),
                (state, move) -> checkCastling(state, move, attackDetector),
                (state, move) -> checkKingSafety(state, move, executor, attackDetector));
    }

    private static ValidationResult checkCoordinates(Move move) {
        if (!move.from().isInsideBoard() || !move.to().isInsideBoard()) {
            return ValidationResult.reject("Move coordinates must be on the board.");
        }
        if (move.from().equals(move.to())) {
            return ValidationResult.reject("A piece cannot move to its current square.");
        }
        return ValidationResult.ok();
    }

    private static ValidationResult checkPieceAndTurn(GameState state, Move move) {
        Piece piece = state.getBoard().getPiece(move.from());
        if (piece == null) return ValidationResult.reject("There is no piece on the source square.");
        if (piece.color() != state.getActiveColor()) {
            return ValidationResult.reject("The piece does not belong to the active player.");
        }
        return ValidationResult.ok();
    }

    private static ValidationResult checkPromotion(GameState state, Move move) {
        Piece piece = state.getBoard().getPiece(move.from());
        boolean reachesLastRank = piece.type() == PieceType.PAWN
                && move.to().row() == piece.color().promotionRow();
        if (reachesLastRank && move.promotionType() == null) {
            return ValidationResult.reject("A pawn reaching the last rank must be promoted.");
        }
        if (!reachesLastRank && move.promotionType() != null) {
            return ValidationResult.reject("Only a pawn reaching the last rank can be promoted.");
        }
        if (move.promotionType() == PieceType.KING || move.promotionType() == PieceType.PAWN) {
            return ValidationResult.reject("A pawn can promote only to a queen, rook, bishop, or knight.");
        }
        return ValidationResult.ok();
    }

    private static ValidationResult checkMovementPattern(GameState state, Move move,
                                                         MoveGenerator generator) {
        if (!generator.generatePseudoLegalMoves(state, move.from()).contains(move)) {
            return ValidationResult.reject("The move does not match the piece's movement rules.");
        }
        return ValidationResult.ok();
    }

    private static ValidationResult checkCastling(GameState state, Move move,
                                                  AttackDetector detector) {
        Piece king = state.getBoard().getPiece(move.from());
        if (king.type() != PieceType.KING
                || Math.abs(move.to().col() - move.from().col()) != 2) {
            return ValidationResult.ok();
        }
        int homeRow = king.color().homeRow();
        boolean kingSide = move.to().col() == 6;
        if (move.from().row() != homeRow || move.from().col() != 4
                || move.to().row() != homeRow
                || (move.to().col() != 2 && move.to().col() != 6)) {
            return ValidationResult.reject("The king is not on its castling square.");
        }
        if (!state.getCastlingRights().canCastle(king.color(), kingSide)) {
            return ValidationResult.reject("Castling rights are not available.");
        }
        int rookCol = kingSide ? 7 : 0;
        Piece rook = state.getBoard().getPiece(new Position(homeRow, rookCol));
        if (rook == null || rook.type() != PieceType.ROOK || rook.color() != king.color()) {
            return ValidationResult.reject("The rook required for castling is not present.");
        }
        if (!castlingPathIsClear(state, homeRow, kingSide)) {
            return ValidationResult.reject("Squares between the king and rook must be empty.");
        }
        if (castlingPathIsAttacked(state, homeRow, kingSide, king.color(), detector)) {
            return ValidationResult.reject("The king cannot castle through or out of check.");
        }
        return ValidationResult.ok();
    }

    private static boolean castlingPathIsClear(GameState state, int row, boolean kingSide) {
        int firstCol = kingSide ? 5 : 1;
        int lastCol = kingSide ? 6 : 3;
        for (int col = firstCol; col <= lastCol; col++) {
            if (!state.getBoard().isEmpty(new Position(row, col))) return false;
        }
        return true;
    }

    private static boolean castlingPathIsAttacked(GameState state, int row, boolean kingSide,
                                                  Color color, AttackDetector detector) {
        Board board = state.getBoard().copy();
        board.setPiece(new Position(row, 4), null);
        int step = kingSide ? 1 : -1;
        for (int col = 4; col != (kingSide ? 7 : 1); col += step) {
            if (detector.isSquareAttacked(board, new Position(row, col), color.opposite())) {
                return true;
            }
        }
        return false;
    }

    private static ValidationResult checkKingSafety(GameState state, Move move,
                                                    MoveExecutor executor,
                                                    AttackDetector detector) {
        Color mover = state.getActiveColor();
        GameState simulation = state.copy();
        executor.applyUnchecked(simulation, move);
        if (detector.isInCheck(simulation.getBoard(), mover)) {
            return ValidationResult.reject("The move leaves the king in check.");
        }
        return ValidationResult.ok();
    }
}
