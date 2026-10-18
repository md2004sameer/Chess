package com.sameer.chess.move.generator;

import java.util.ArrayList;
import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.move.Move;

public class KingMoveGenerator implements MoveGenerator {

    private static final int[][] OFFSETS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1},             {0, 1},
        {1, -1},  {1, 0},  {1, 1}
    };

    @Override
    public List<Move> generateMoves(Position from, BoardReader board) {
        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(from);
        if (piece == null) {
            return moves;
        }

        for (int[] offset : OFFSETS) {
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
