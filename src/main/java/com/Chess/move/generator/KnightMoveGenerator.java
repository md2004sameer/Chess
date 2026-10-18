package com.Chess.move.generator;

import java.util.ArrayList;
import java.util.List;

import com.Chess.board.Board;
import com.Chess.model.Piece;
import com.Chess.model.Position;
import com.Chess.move.Move;

public class KnightMoveGenerator implements MoveGenerator {

    private static final int[][] OFFSETS = {
        {-2, -1}, {-2, 1},
        {-1, -2}, {-1, 2},
        {1, -2}, {1, 2},
        {2, -1}, {2, 1}
    };

    @Override
    public List<Move> generateMoves(Piece piece, Board board) {
        List<Move> moves = new ArrayList<>();
        Position from = piece.getPosition();

        for (int[] offset : OFFSETS) {
            int row = from.getRow() + offset[0];
            int col = from.getCol() + offset[1];
            if (row < 0 || row >= 8 || col < 0 || col >= 8) {
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
