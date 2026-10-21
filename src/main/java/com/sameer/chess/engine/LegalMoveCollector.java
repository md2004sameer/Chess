package com.sameer.chess.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.movegen.MoveGenerator;

/** Collects legal candidates once per piece and stops early when only existence is needed. */
final class LegalMoveCollector {
    private final MoveGenerator generator;
    private final BiPredicate<GameState, Move> legalCandidate;

    LegalMoveCollector(MoveGenerator generator, BiPredicate<GameState, Move> legalCandidate) {
        this.generator = generator;
        this.legalCandidate = legalCandidate;
    }

    List<Move> all(GameState state) {
        List<Move> moves = new ArrayList<>();
        collect(state, null, moves);
        return List.copyOf(moves);
    }

    List<Move> from(GameState state, Position from) {
        if (from == null) return List.of();
        List<Move> moves = new ArrayList<>();
        collect(state, from, moves);
        return List.copyOf(moves);
    }

    boolean hasAny(GameState state) {
        return collect(state, null, null);
    }

    private boolean collect(GameState state, Position onlyFrom, List<Move> moves) {
        if (state == null || (onlyFrom != null && !state.getBoard().isInside(onlyFrom))) {
            return false;
        }
        int firstRow = onlyFrom == null ? 0 : onlyFrom.row();
        int lastRow = onlyFrom == null ? 7 : onlyFrom.row();
        int firstCol = onlyFrom == null ? 0 : onlyFrom.col();
        int lastCol = onlyFrom == null ? 7 : onlyFrom.col();
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {
                if (collectFrom(state, new Position(row, col), moves)) return true;
            }
        }
        return false;
    }

    private boolean collectFrom(GameState state, Position from, List<Move> moves) {
        Piece piece = state.getBoard().getPiece(from);
        if (piece == null || piece.color() != state.getActiveColor()) return false;
        for (Move candidate : generator.generatePseudoLegalMoves(state, from)) {
            if (!legalCandidate.test(state, candidate)) continue;
            if (moves == null) return true;
            moves.add(candidate);
        }
        return false;
    }
}
