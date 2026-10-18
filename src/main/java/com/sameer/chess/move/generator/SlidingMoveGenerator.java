package com.sameer.chess.move.generator;

import java.util.ArrayList;
import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.move.Move;

abstract class SlidingMoveGenerator implements MoveGenerator {

    protected abstract int[][] directions();

    @Override
    public List<Move> generateMoves(Position from, BoardReader board) {
        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(from);
        if (piece == null) {
            return moves;
        }

        for (int[] direction : directions()) {
            int row = from.getRow();
            int col = from.getCol();

            while (true) {
                row += direction[0];
                col += direction[1];
                if (!Position.isOnBoard(row, col)) {
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
}
