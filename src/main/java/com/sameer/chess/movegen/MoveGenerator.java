package com.sameer.chess.movegen;

import java.util.ArrayList;
import java.util.List;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

/** Generates pseudo-legal candidates; king safety is enforced by MoveValidator. */
public final class MoveGenerator {
    private static final PieceType[] PROMOTIONS = {
            PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT
    };
    private static final int[][] ROOK_DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };
    private static final int[][] BISHOP_DIRECTIONS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };
    private static final int[][] KNIGHT_OFFSETS = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    public List<Move> generatePseudoLegalMoves(GameState state, Position from) {
        if (!state.getBoard().isInside(from)) return List.of();
        Piece piece = state.getBoard().getPiece(from);
        if (piece == null) return List.of();
        return switch (piece.type()) {
            case KING -> kingMoves(state, from, piece);
            case QUEEN -> slidingMoves(state, from, piece, combine(ROOK_DIRECTIONS, BISHOP_DIRECTIONS));
            case ROOK -> slidingMoves(state, from, piece, ROOK_DIRECTIONS);
            case BISHOP -> slidingMoves(state, from, piece, BISHOP_DIRECTIONS);
            case KNIGHT -> knightMoves(state, from, piece);
            case PAWN -> pawnMoves(state, from, piece);
        };
    }

    private List<Move> slidingMoves(GameState state, Position from, Piece piece, int[][] directions) {
        List<Move> moves = new ArrayList<>();
        Board board = state.getBoard();
        for (int[] direction : directions) {
            int row = from.row() + direction[0];
            int col = from.col() + direction[1];
            while (inside(row, col)) {
                Position to = new Position(row, col);
                Piece target = board.getPiece(to);
                if (target == null) {
                    moves.add(new Move(from, to, null));
                } else {
                    if (target.color() != piece.color() && target.type() != PieceType.KING) {
                        moves.add(new Move(from, to, null));
                    }
                    break;
                }
                row += direction[0];
                col += direction[1];
            }
        }
        return moves;
    }

    private List<Move> knightMoves(GameState state, Position from, Piece piece) {
        List<Move> moves = new ArrayList<>();
        for (int[] offset : KNIGHT_OFFSETS) {
            int row = from.row() + offset[0];
            int col = from.col() + offset[1];
            if (!inside(row, col)) continue;
            Position to = new Position(row, col);
            Piece target = state.getBoard().getPiece(to);
            if (target == null || (target.color() != piece.color()
                    && target.type() != PieceType.KING)) {
                moves.add(new Move(from, to, null));
            }
        }
        return moves;
    }

    private List<Move> kingMoves(GameState state, Position from, Piece king) {
        List<Move> moves = new ArrayList<>();
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {
                if (rowOffset == 0 && colOffset == 0) continue;
                int row = from.row() + rowOffset;
                int col = from.col() + colOffset;
                if (!inside(row, col)) continue;
                Position to = new Position(row, col);
                Piece target = state.getBoard().getPiece(to);
                if (target == null || (target.color() != king.color()
                        && target.type() != PieceType.KING)) {
                    moves.add(new Move(from, to, null));
                }
            }
        }

        int homeRow = king.color().homeRow();
        if (from.row() == homeRow && from.col() == 4) {
            if (state.getCastlingRights().canCastle(king.color(), true)) {
                moves.add(new Move(from, new Position(homeRow, 6), null));
            }
            if (state.getCastlingRights().canCastle(king.color(), false)) {
                moves.add(new Move(from, new Position(homeRow, 2), null));
            }
        }
        return moves;
    }

    private List<Move> pawnMoves(GameState state, Position from, Piece pawn) {
        List<Move> moves = new ArrayList<>();
        Board board = state.getBoard();
        int direction = pawn.color().pawnDirection();
        int oneRow = from.row() + direction;
        if (inside(oneRow, from.col())) {
            Position oneForward = new Position(oneRow, from.col());
            if (board.isEmpty(oneForward)) {
                addPawnMove(moves, from, oneForward, pawn.color());
                int twoRow = from.row() + 2 * direction;
                if (from.row() == pawn.color().pawnStartRow() && inside(twoRow, from.col())) {
                    Position twoForward = new Position(twoRow, from.col());
                    if (board.isEmpty(twoForward)) moves.add(new Move(from, twoForward, null));
                }
            }
        }

        for (int colOffset : new int[]{-1, 1}) {
            int targetRow = from.row() + direction;
            int targetCol = from.col() + colOffset;
            if (!inside(targetRow, targetCol)) continue;
            Position target = new Position(targetRow, targetCol);
            Piece occupying = board.getPiece(target);
            if (occupying != null && occupying.color() != pawn.color()
                    && occupying.type() != PieceType.KING) {
                addPawnMove(moves, from, target, pawn.color());
            } else if (occupying == null && target.equals(state.getEnPassantTarget())) {
                Piece captured = board.getPiece(new Position(from.row(), targetCol));
                if (captured != null && captured.type() == PieceType.PAWN
                        && captured.color() != pawn.color()) {
                    moves.add(new Move(from, target, null));
                }
            }
        }
        return moves;
    }

    private void addPawnMove(List<Move> moves, Position from, Position to, Color color) {
        if (to.row() == color.promotionRow()) {
            for (PieceType promotion : PROMOTIONS) moves.add(new Move(from, to, promotion));
        } else {
            moves.add(new Move(from, to, null));
        }
    }

    private static int[][] combine(int[][] first, int[][] second) {
        int[][] combined = new int[first.length + second.length][];
        System.arraycopy(first, 0, combined, 0, first.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }

    private static boolean inside(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
