package com.sameer.chess.engine;

import com.sameer.chess.board.Board;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

/** Detects attacked squares and check without relying on legal move generation. */
public final class AttackDetector {
    public boolean isSquareAttacked(Board board, Position target, Color attacker) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position from = new Position(row, col);
                Piece piece = board.getPiece(from);
                if (piece != null && piece.color() == attacker
                        && attacksSquare(board, from, target, piece)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attacksSquare(Board board, Position from, Position target, Piece piece) {
        int rowDelta = target.row() - from.row();
        int colDelta = target.col() - from.col();
        int absRow = Math.abs(rowDelta);
        int absCol = Math.abs(colDelta);

        return switch (piece.type()) {
            case PAWN -> rowDelta == piece.color().pawnDirection() && absCol == 1;
            case KNIGHT -> absRow * absCol == 2;
            case KING -> Math.max(absRow, absCol) == 1;
            case BISHOP -> absRow == absCol && absRow > 0 && clearRay(board, from, target);
            case ROOK -> ((rowDelta == 0) != (colDelta == 0)) && clearRay(board, from, target);
            case QUEEN -> ((absRow == absCol && absRow > 0)
                    || ((rowDelta == 0) != (colDelta == 0))) && clearRay(board, from, target);
        };
    }

    private boolean clearRay(Board board, Position from, Position target) {
        int rowStep = Integer.compare(target.row(), from.row());
        int colStep = Integer.compare(target.col(), from.col());
        int row = from.row() + rowStep;
        int col = from.col() + colStep;
        while (row != target.row() || col != target.col()) {
            if (board.getPiece(new Position(row, col)) != null) return false;
            row += rowStep;
            col += colStep;
        }
        return true;
    }

    public Position findKing(Board board, Color color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position position = new Position(row, col);
                Piece piece = board.getPiece(position);
                if (piece != null && piece.color() == color && piece.type() == PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }

    public boolean isInCheck(Board board, Color color) {
        Position king = findKing(board, color);
        return king != null && isSquareAttacked(board, king, color.opposite());
    }
}
