package com.sameer.chess.movegen;

import java.util.ArrayList;
import java.util.List;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

/** Generates pawn moves independently from other piece movement rules. */
public final class PawnMoveGenerator implements PieceMoveGenerator {
    private static final PieceType[] PROMOTIONS = {
            PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT
    };

    @Override
    public List<Move> generate(GameState state, Position from) {
        List<Move> moves = new ArrayList<>();
        Piece pawn = state.getBoard().getPiece(from);
        addForwardMoves(state.getBoard(), from, pawn, moves);
        addCaptures(state, from, pawn, moves);
        return moves;
    }

    private void addForwardMoves(Board board, Position from, Piece pawn, List<Move> moves) {
        int oneRow = from.row() + pawn.color().pawnDirection();
        if (!inside(oneRow, from.col())) return;
        Position oneForward = new Position(oneRow, from.col());
        if (!board.isEmpty(oneForward)) return;
        addPawnMove(moves, from, oneForward, pawn.color());
        addDoublePush(board, from, pawn, moves);
    }

    private void addDoublePush(Board board, Position from, Piece pawn, List<Move> moves) {
        int twoRow = from.row() + 2 * pawn.color().pawnDirection();
        if (from.row() != pawn.color().pawnStartRow() || !inside(twoRow, from.col())) return;
        Position twoForward = new Position(twoRow, from.col());
        if (board.isEmpty(twoForward)) moves.add(new Move(from, twoForward, null));
    }

    private void addCaptures(GameState state, Position from, Piece pawn, List<Move> moves) {
        for (int colOffset : new int[]{-1, 1}) {
            addCapture(state, from, pawn, colOffset, moves);
        }
    }

    private void addCapture(GameState state, Position from, Piece pawn, int colOffset,
                            List<Move> moves) {
        int targetRow = from.row() + pawn.color().pawnDirection();
        int targetCol = from.col() + colOffset;
        if (!inside(targetRow, targetCol)) return;
        Position target = new Position(targetRow, targetCol);
        Piece occupying = state.getBoard().getPiece(target);
        if (isCapturablePiece(occupying, pawn.color())) {
            addPawnMove(moves, from, target, pawn.color());
        } else if (occupying == null && target.equals(state.getEnPassantTarget())) {
            addEnPassant(state, from, pawn, target, moves);
        }
    }

    private boolean isCapturablePiece(Piece target, Color mover) {
        return target != null && target.color() != mover && target.type() != PieceType.KING;
    }

    private void addEnPassant(GameState state, Position from, Piece pawn,
                              Position target, List<Move> moves) {
        Piece captured = state.getBoard().getPiece(new Position(from.row(), target.col()));
        if (captured != null && captured.type() == PieceType.PAWN
                && captured.color() != pawn.color()) {
            moves.add(new Move(from, target, null));
        }
    }

    private void addPawnMove(List<Move> moves, Position from, Position to, Color color) {
        if (to.row() == color.promotionRow()) {
            for (PieceType promotion : PROMOTIONS) moves.add(new Move(from, to, promotion));
        } else {
            moves.add(new Move(from, to, null));
        }
    }

    private boolean inside(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
