package com.sameer.chess.move.rule;

import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.move.Move;

/** Adds or transforms pseudo-legal candidates without changing board placement. */
public interface SpecialMoveRule {

    List<Move> apply(
            Piece piece,
            Position from,
            List<Move> candidates,
            BoardReader board,
            RuleContext ruleContext);
}
