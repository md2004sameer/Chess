package com.sameer.chess.move.generator;

import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.move.Move;

abstract class AbstractMoveGenerator implements MoveGenerator {

    @Override
    public final List<Move> generateMoves(Position from, BoardReader board) {
        Piece piece = board.getPiece(from);
        if (piece == null || piece.getType() != type()) {
            return List.of();
        }
        return generatePieceMoves(from, board, piece);
    }

    protected abstract List<Move> generatePieceMoves(Position from, BoardReader board, Piece piece);

    protected final boolean canLandOn(BoardReader board, Position to, Piece movingPiece) {
        Piece target = board.getPiece(to);
        return target == null || isEnemy(movingPiece, target);
    }

    protected final boolean isEnemy(Piece movingPiece, Piece target) {
        return target != null && target.getColor() != movingPiece.getColor();
    }
}
