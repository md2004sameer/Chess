package com.sameer.chess.movegen;

import java.util.ArrayList;
import java.util.List;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

/** Generates king moves independently from other piece movement rules. */
public final class KingMoveGenerator implements PieceMoveGenerator {
    @Override
    public List<Move> generate(GameState state, Position from) {
        List<Move> moves = new ArrayList<>();
        Board board = state.getBoard();
        Piece king = board.getPiece(from);
        addNeighborMoves(board, from, king, moves);
        addCastlingCandidates(state, from, king, moves);
        return moves;
    }

    private void addNeighborMoves(Board board, Position from, Piece king, List<Move> moves) {
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {
                addNeighborMove(board, from, king, rowOffset, colOffset, moves);
            }
        }
    }

    private void addNeighborMove(Board board, Position from, Piece king, int rowOffset,
                                 int colOffset, List<Move> moves) {
        if (rowOffset == 0 && colOffset == 0) return;
        int row = from.row() + rowOffset;
        int col = from.col() + colOffset;
        if (!inside(row, col)) return;
        Position to = new Position(row, col);
        Piece target = board.getPiece(to);
        if (target == null || (target.color() != king.color()
                && target.type() != PieceType.KING)) {
            moves.add(new Move(from, to, null));
        }
    }

    private void addCastlingCandidates(GameState state, Position from, Piece king,
                                       List<Move> moves) {
        int homeRow = king.color().homeRow();
        if (from.row() != homeRow || from.col() != 4) return;
        if (state.getCastlingRights().canCastle(king.color(), true)) {
            moves.add(new Move(from, new Position(homeRow, 6), null));
        }
        if (state.getCastlingRights().canCastle(king.color(), false)) {
            moves.add(new Move(from, new Position(homeRow, 2), null));
        }
    }

    private boolean inside(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
