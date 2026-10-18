package com.Chess.move.generator;

import java.util.ArrayList;
import java.util.List;

import com.Chess.board.Board;
import com.Chess.model.Piece;
import com.Chess.model.Position;
import com.Chess.model.enums.Color;
import com.Chess.move.Move;

public class PawnMoveGenerator implements MoveGenerator {

    @Override
    public List<Move> generateMoves(Piece piece, Board board) {
        List<Move> moves = new ArrayList<>();
        Position from = piece.getPosition();
        int direction = piece.getColor() == Color.WHITE ? -1 : 1;
        int nextRow = from.getRow() + direction;

        if (isOnBoard(nextRow, from.getCol())
                && board.getPiece(new Position(nextRow, from.getCol())) == null) {
            moves.add(new Move(from, new Position(nextRow, from.getCol())));

            int startingRow = piece.getColor() == Color.WHITE ? 6 : 1;
            int doubleStepRow = from.getRow() + 2 * direction;
            if (from.getRow() == startingRow
                    && board.getPiece(new Position(doubleStepRow, from.getCol())) == null) {
                moves.add(new Move(from, new Position(doubleStepRow, from.getCol())));
            }
        }

        for (int colOffset : new int[]{-1, 1}) {
            int captureRow = from.getRow() + direction;
            int captureCol = from.getCol() + colOffset;
            if (!isOnBoard(captureRow, captureCol)) {
                continue;
            }

            Position to = new Position(captureRow, captureCol);
            Piece target = board.getPiece(to);
            if (target != null && target.getColor() != piece.getColor()) {
                moves.add(new Move(from, to));
            }
        }

        return moves;
    }

    private boolean isOnBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
