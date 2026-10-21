package com.sameer.chess.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.ValidationResult;
import com.sameer.chess.movegen.MoveGenerator;

/** Validates chess moves and enumerates legal moves for a position. */
public final class MoveValidator {
    private final MoveGenerator generator;
    private final List<MoveRule> rules;

    public MoveValidator(MoveGenerator generator, List<MoveRule> rules) {
        this.generator = Objects.requireNonNull(generator, "generator");
        this.rules = List.copyOf(rules);
    }

    public static MoveValidator standard(MoveGenerator generator, MoveExecutor executor,
                                         AttackDetector attackDetector) {
        return new MoveValidator(generator,
                StandardMoveRules.create(generator, executor, attackDetector));
    }

    public ValidationResult validate(GameState state, Move move) {
        if (state == null || move == null) return ValidationResult.reject("State and move are required.");
        for (MoveRule rule : rules) {
            ValidationResult result = rule.validate(state, move);
            if (!result.valid()) return result;
        }
        return ValidationResult.ok();
    }

    public List<Move> generateLegalMoves(GameState state) {
        List<Move> legalMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position from = new Position(row, col);
                Piece piece = state.getBoard().getPiece(from);
                if (piece == null || piece.color() != state.getActiveColor()) continue;
                for (Move candidate : generator.generatePseudoLegalMoves(state, from)) {
                    if (validate(state, candidate).valid()) legalMoves.add(candidate);
                }
            }
        }
        return List.copyOf(legalMoves);
    }

    public boolean hasAnyLegalMove(GameState state) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position from = new Position(row, col);
                Piece piece = state.getBoard().getPiece(from);
                if (piece == null || piece.color() != state.getActiveColor()) continue;
                for (Move candidate : generator.generatePseudoLegalMoves(state, from)) {
                    if (validate(state, candidate).valid()) return true;
                }
            }
        }
        return false;
    }
}
