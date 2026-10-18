package com.sameer.chess.move.rule;

import java.util.ArrayList;
import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;
import com.sameer.chess.move.MoveKind;

public class EnPassantRule implements SpecialMoveRule {

    @Override
    public List<Move> apply(
            Piece piece,
            Position from,
            List<Move> candidates,
            BoardReader board,
            RuleContext ruleContext) {
        if (piece.getType() != Type.PAWN) {
            return candidates;
        }
        List<Move> result = new ArrayList<>(candidates);
        int direction = piece.getColor().forward();
        int targetRow = from.row() + direction;
        for (int colOffset : new int[]{-1, 1}) {
            int targetCol = from.col() + colOffset;
            if (!Position.isOnBoard(targetRow, targetCol)) {
                continue;
            }
            Position target = new Position(targetRow, targetCol);
            Piece pawnAtTargetFile = board.getPiece(new Position(from.row(), targetCol));
            if (!board.isOccupied(target)
                    && ruleContext.canCaptureEnPassant(from, target, piece.getColor(), pawnAtTargetFile)) {
                result.add(new Move(from, target, null, MoveKind.EN_PASSANT));
            }
        }
        return List.copyOf(result);
    }
}
