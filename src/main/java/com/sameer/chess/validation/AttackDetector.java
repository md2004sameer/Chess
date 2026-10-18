package com.sameer.chess.validation;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;

@Component
public class AttackDetector {

    public boolean isSquareAttacked(BoardReader board, Position target, Color attackingColor) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(target, "target position must not be null");
        Objects.requireNonNull(attackingColor, "attacking color must not be null");

        for (Position from : board.getOccupiedPositions()) {
            Piece piece = board.getPiece(from);
            if (piece.getColor() == attackingColor && attacksSquare(piece, from, target, board)) {
                return true;
            }
        }
        return false;
    }

    private boolean attacksSquare(Piece piece, Position from, Position target, BoardReader board) {
        if (from.equals(target)) {
            return false;
        }
        int rowDifference = target.getRow() - from.getRow();
        int colDifference = target.getCol() - from.getCol();
        int absRowDifference = Math.abs(rowDifference);
        int absColDifference = Math.abs(colDifference);

        return switch (piece.getType()) {
            case KING -> Math.max(absRowDifference, absColDifference) == 1;
            case KNIGHT -> absRowDifference * absColDifference == 2;
            case PAWN -> rowDifference == (piece.getColor() == Color.WHITE ? -1 : 1)
                    && absColDifference == 1;
            case ROOK -> (rowDifference == 0 || colDifference == 0)
                    && isPathClear(from, target, board);
            case BISHOP -> absRowDifference == absColDifference
                    && isPathClear(from, target, board);
            case QUEEN -> (rowDifference == 0 || colDifference == 0
                    || absRowDifference == absColDifference)
                    && isPathClear(from, target, board);
        };
    }

    private boolean isPathClear(Position from, Position to, BoardReader board) {
        int rowStep = Integer.compare(to.getRow(), from.getRow());
        int colStep = Integer.compare(to.getCol(), from.getCol());
        int row = from.getRow() + rowStep;
        int col = from.getCol() + colStep;

        while (row != to.getRow() || col != to.getCol()) {
            if (board.getPiece(new Position(row, col)) != null) {
                return false;
            }
            row += rowStep;
            col += colStep;
        }
        return true;
    }
}
