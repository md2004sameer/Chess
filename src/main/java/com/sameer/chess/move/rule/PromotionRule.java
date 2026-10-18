package com.sameer.chess.move.rule;

import java.util.ArrayList;
import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;
import com.sameer.chess.move.MoveKind;

public class PromotionRule implements SpecialMoveRule {

    private static final Type[] PROMOTION_TYPES = {
        Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT
    };

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
        int promotionRow = piece.getColor().promotionRow();
        List<Move> result = new ArrayList<>();
        for (Move candidate : candidates) {
            if (candidate.to().row() == promotionRow) {
                for (Type promotionType : PROMOTION_TYPES) {
                    result.add(new Move(
                            from, candidate.to(), promotionType, MoveKind.PROMOTION));
                }
            } else {
                result.add(candidate);
            }
        }
        return List.copyOf(result);
    }
}
