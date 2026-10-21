package com.sameer.chess.movegen;

import java.util.List;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;

/** Dispatches pseudo-legal move requests to the generator for the piece on the square. */
public final class MoveGenerator {
    public List<Move> generatePseudoLegalMoves(GameState state, Position from) {
        if (!state.getBoard().isInside(from)) return List.of();
        Piece piece = state.getBoard().getPiece(from);
        if (piece == null) return List.of();
        return MoveGeneratorFactory.forPiece(piece.type()).generate(state, from);
    }
}
