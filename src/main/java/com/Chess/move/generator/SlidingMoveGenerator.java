package com.Chess.move.generator;

import java.util.ArrayList;
import java.util.List;

import com.Chess.board.Board;
import com.Chess.model.Piece;
import com.Chess.model.Position;
import com.Chess.move.Move;

abstract class SlidingMoveGenerator implements MoveGenerator {

    protected abstract int[][] directions();

    @Override
    public List<Move> generateMoves(Piece piece, Board board) {
        List<Move> moves = new ArrayList<>();
        Position from = piece.getPosition();

        for (int[] direction : directions()) {
            int row = from.getRow();
            int col = from.getCol();

            while (true) {
                row += direction[0];
                col += direction[1];
                if (!isOnBoard(row, col)) {
                    break;
                }

                Position to = new Position(row, col);
                Piece target = board.getPiece(to);
                if (target == null) {
                    moves.add(new Move(from, to));
                } else {
                    if (target.getColor() != piece.getColor()) {
                        moves.add(new Move(from, to));
                    }
                    break;
                }
            }
        }

        return moves;
    }

    private boolean isOnBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
