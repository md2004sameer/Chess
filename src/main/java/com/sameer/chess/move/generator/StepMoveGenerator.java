package com.sameer.chess.move.generator;

import java.util.ArrayList;
import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.move.Move;

abstract class StepMoveGenerator extends AbstractMoveGenerator {

    protected abstract List<Direction> directions();

    @Override
    protected final List<Move> generatePieceMoves(Position from, BoardReader board, Piece piece) {
        List<Move> moves = new ArrayList<>();

        for (Direction direction : directions()) {
            int row = from.row() + direction.dRow();
            int col = from.col() + direction.dCol();
            if (!Position.isOnBoard(row, col)) {
                continue;
            }

            Position to = new Position(row, col);
            if (canLandOn(board, to, piece)) {
                moves.add(new Move(from, to));
            }
        }

        return moves;
    }
}
