package com.sameer.chess.move.rule;

import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;

public interface RuleContext {

    boolean canCastle(Color color, boolean kingSide);

    boolean canCaptureEnPassant(Position from, Position to, Color color, Piece pawnAtTarget);
}
