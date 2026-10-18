package com.sameer.chess.move.generator;

import java.util.ArrayList;
import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.move.Move;

abstract class StepMoveGenerator implements MoveGenerator {

    protected abstract int[][] offsets();

    @Override
    public List<Move> generateMoves(Position from, BoardReader board) {
        Piece piece = board.getPiece(from);
        if (piece == null || piece.getType() != type()) {
            return List.of();
        }
        List<Move> moves = new ArrayList<>();

        for (int[] offset : offsets()) {
            int row = from.getRow() + offset[0];
            int col = from.getCol() + offset[1];
            if (!Position.isOnBoard(row, col)) {
                continue;
            }

            Position to = new Position(row, col);
            Piece target = board.getPiece(to);
            if (target == null || target.getColor() != piece.getColor()) {
                moves.add(new Move(from, to));
            }
        }

        return moves;
    }
}
