package com.sameer.chess.movegen;

import java.util.ArrayList;
import java.util.List;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Position;

/** Shares sliding-piece move traversal so rook, bishop, and queen rules can reuse it. */
public final class SlidingMoves {
    private SlidingMoves() {
    }

    public static List<Move> generate(GameState state, Position from, int[][] directions) {
        List<Move> moves = new ArrayList<>();
        Board board = state.getBoard();
        Piece piece = board.getPiece(from);
        for (int[] direction : directions) {
            addRayMoves(board, from, piece, direction, moves);
        }
        return moves;
    }

    private static void addRayMoves(Board board, Position from, Piece piece,
                                    int[] direction, List<Move> moves) {
        int row = from.row() + direction[0];
        int col = from.col() + direction[1];
        while (inside(row, col)) {
            Position to = new Position(row, col);
            Piece target = board.getPiece(to);
            if (target == null) {
                moves.add(new Move(from, to, null));
            } else {
                addCaptureIfAllowed(moves, from, to, piece, target);
                break;
            }
            row += direction[0];
            col += direction[1];
        }
    }

    private static void addCaptureIfAllowed(List<Move> moves, Position from, Position to,
                                            Piece piece, Piece target) {
        if (target.color() != piece.color() && target.type() != PieceType.KING) {
            moves.add(new Move(from, to, null));
        }
    }

    private static boolean inside(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
