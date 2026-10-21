package com.sameer.chess.movegen;

import java.util.ArrayList;
import java.util.List;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

/** Generates knight moves independently from other piece movement rules. */
public final class KnightMoveGenerator implements PieceMoveGenerator {
    private static final int[][] OFFSETS = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };

    @Override
    public List<Move> generate(GameState state, Position from) {
        List<Move> moves = new ArrayList<>();
        Board board = state.getBoard();
        Piece piece = board.getPiece(from);
        for (int[] offset : OFFSETS) {
            addMoveIfAvailable(board, from, piece, offset, moves);
        }
        return moves;
    }

    private void addMoveIfAvailable(Board board, Position from, Piece piece,
                                    int[] offset, List<Move> moves) {
        int row = from.row() + offset[0];
        int col = from.col() + offset[1];
        if (!inside(row, col)) return;
        Position to = new Position(row, col);
        Piece target = board.getPiece(to);
        if (target == null || (target.color() != piece.color()
                && target.type() != PieceType.KING)) {
            moves.add(new Move(from, to, null));
        }
    }

    private boolean inside(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
