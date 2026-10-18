package com.sameer.chess.move.generator;

import java.util.ArrayList;
import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;

public class PawnMoveGenerator implements MoveGenerator {

    @Override
    public Type type() {
        return Type.PAWN;
    }

    @Override
    public List<Move> generateMoves(Position from, BoardReader board) {
        Piece piece = board.getPiece(from);
        if (piece == null || piece.getType() != type()) {
            return List.of();
        }
        List<Move> moves = new ArrayList<>();
        int direction = piece.getColor().forward();
        int nextRow = from.getRow() + direction;

        if (Position.isOnBoard(nextRow, from.getCol())
                && board.getPiece(new Position(nextRow, from.getCol())) == null) {
            moves.add(new Move(from, new Position(nextRow, from.getCol())));

            int startingRow = piece.getColor().pawnRow();
            int doubleStepRow = from.getRow() + 2 * direction;
            if (from.getRow() == startingRow
                    && board.getPiece(new Position(doubleStepRow, from.getCol())) == null) {
                moves.add(new Move(from, new Position(doubleStepRow, from.getCol())));
            }
        }

        for (int colOffset : new int[]{-1, 1}) {
            int captureRow = from.getRow() + direction;
            int captureCol = from.getCol() + colOffset;
            if (!Position.isOnBoard(captureRow, captureCol)) {
                continue;
            }

            Position to = new Position(captureRow, captureCol);
            Piece target = board.getPiece(to);
            if (target != null && target.getColor() != piece.getColor()) {
                moves.add(new Move(from, to));
            }
        }

        return moves;
    }
}
