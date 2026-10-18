package com.sameer.chess.move.generator;

import java.util.ArrayList;
import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;

public class PawnMoveGenerator extends AbstractMoveGenerator {

    @Override
    public Type type() {
        return Type.PAWN;
    }

    @Override
    protected List<Move> generatePieceMoves(Position from, BoardReader board, Piece piece) {
        List<Move> moves = new ArrayList<>();
        int direction = piece.getColor().forward();
        int nextRow = from.row() + direction;

        if (Position.isOnBoard(nextRow, from.col())
                && board.getPiece(new Position(nextRow, from.col())) == null) {
            moves.add(new Move(from, new Position(nextRow, from.col())));

            int startingRow = piece.getColor().pawnRow();
            int doubleStepRow = from.row() + 2 * direction;
            if (from.row() == startingRow
                    && board.getPiece(new Position(doubleStepRow, from.col())) == null) {
                moves.add(new Move(from, new Position(doubleStepRow, from.col())));
            }
        }

        for (int colOffset : new int[]{-1, 1}) {
            int captureRow = from.row() + direction;
            int captureCol = from.col() + colOffset;
            if (!Position.isOnBoard(captureRow, captureCol)) {
                continue;
            }

            Position to = new Position(captureRow, captureCol);
            Piece target = board.getPiece(to);
            if (isEnemy(piece, target)) {
                moves.add(new Move(from, to));
            }
        }

        return moves;
    }
}
