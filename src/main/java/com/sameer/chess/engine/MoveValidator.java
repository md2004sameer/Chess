package com.sameer.chess.engine;

import java.util.List;
import java.util.Objects;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.ValidationResult;
import com.sameer.chess.movegen.MoveGenerator;

/** Validates chess moves and efficiently enumerates legal candidates for a position. */
public final class MoveValidator {
    private final MoveGenerator generator;
    private final MoveExecutor executor;
    private final AttackDetector attackDetector;
    private final CastlingChecker castlingChecker;
    private final LegalMoveCollector legalMoveCollector;

    public MoveValidator(MoveGenerator generator, MoveExecutor executor, AttackDetector attackDetector) {
        this.generator = Objects.requireNonNull(generator, "generator");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.attackDetector = Objects.requireNonNull(attackDetector, "attackDetector");
        castlingChecker = new CastlingChecker(attackDetector);
        legalMoveCollector = new LegalMoveCollector(generator, this::isLegalCandidate);
    }

    public static MoveValidator standard(MoveGenerator generator, MoveExecutor executor,
            AttackDetector attackDetector) {
        return new MoveValidator(generator, executor, attackDetector);
    }

    public ValidationResult validate(GameState state, Move move) {
        if (state == null || move == null) {
            return ValidationResult.reject("State and move are required.");
        }
        ValidationResult result = checkCoordinates(state, move);
        if (!result.valid()) return result;
        result = checkPieceAndTurn(state, move);
        if (!result.valid()) return result;
        result = checkPromotion(state, move);
        if (!result.valid()) return result;
        result = checkMovementPattern(state, move);
        if (!result.valid()) return result;
        result = checkCastling(state, move);
        if (!result.valid()) return result;
        return checkKingSafety(state, move);
    }

    public List<Move> legalMoves(GameState state) {
        return legalMoveCollector.all(state);
    }

    public List<Move> generateLegalMoves(GameState state) { return legalMoves(state); }

    public List<Move> legalMovesFrom(GameState state, Position from) {
        return legalMoveCollector.from(state, from);
    }

    public boolean hasAnyLegalMove(GameState state) { return legalMoveCollector.hasAny(state); }

    // Reject invalid squares before any piece lookup can touch the board.
    private ValidationResult checkCoordinates(GameState state, Move move) {
        if (state == null || move == null || !state.getBoard().isInside(move.from())
                || !state.getBoard().isInside(move.to())) {
            return ValidationResult.reject("Move coordinates must be on the board.");
        }
        if (move.from().equals(move.to())) {
            return ValidationResult.reject("A piece cannot move to its current square.");
        }
        return ValidationResult.ok();
    }

    // Keep moves restricted to a piece owned by the side to move.
    private ValidationResult checkPieceAndTurn(GameState state, Move move) {
        if (state == null || move == null || !state.getBoard().isInside(move.from())) {
            return ValidationResult.reject("There is no piece on the source square.");
        }
        Piece piece = state.getBoard().getPiece(move.from());
        if (piece == null) return ValidationResult.reject("There is no piece on the source square.");
        if (piece.color() != state.getActiveColor()) {
            return ValidationResult.reject("The piece does not belong to the active player.");
        }
        return ValidationResult.ok();
    }

    // Give promotion mistakes a useful reason before generic movement rejection.
    private ValidationResult checkPromotion(GameState state, Move move) {
        Piece piece = pieceAt(state, move == null ? null : move.from());
        if (piece == null || move == null) return ValidationResult.reject(
                "There is no piece on the source square.");
        boolean reachesLastRank = piece.type() == PieceType.PAWN
                && move.to().row() == piece.color().promotionRow();
        if (reachesLastRank && move.promotionType() == null) {
            return ValidationResult.reject("A pawn reaching the last rank must be promoted.");
        }
        if (!reachesLastRank && move.promotionType() != null) {
            return ValidationResult.reject("Only a pawn reaching the last rank can be promoted.");
        }
        if (move.promotionType() == PieceType.KING || move.promotionType() == PieceType.PAWN) {
            return ValidationResult.reject(
                    "A pawn can promote only to a queen, rook, bishop, or knight.");
        }
        return ValidationResult.ok();
    }

    // Reject moves that do not follow the selected piece's pseudo-legal movement.
    private ValidationResult checkMovementPattern(GameState state, Move move) {
        if (state == null || move == null || pieceAt(state, move.from()) == null
                || !state.getBoard().isInside(move.to())) {
            return ValidationResult.reject("The move does not match the piece's movement rules.");
        }
        if (!generator.generatePseudoLegalMoves(state, move.from()).contains(move)) {
            return ValidationResult.reject("The move does not match the piece's movement rules.");
        }
        return ValidationResult.ok();
    }

    // Keep castling-specific rights and attacked-path checks out of ordinary moves.
    private ValidationResult checkCastling(GameState state, Move move) { return castlingChecker.validate(state, move); }

    // Simulate on a copy so rejected moves cannot expose or mutate the live position.
    private ValidationResult checkKingSafety(GameState state, Move move) {
        Piece movingPiece = pieceAt(state, move == null ? null : move.from());
        if (state == null || move == null || movingPiece == null
                || !state.getBoard().isInside(move.to())) {
            return ValidationResult.reject("The move cannot be simulated safely.");
        }
        Color mover = state.getActiveColor();
        GameState simulation = state.copy();
        executor.applyUnchecked(simulation, move);
        if (attackDetector.isInCheck(simulation.getBoard(), mover)) {
            return ValidationResult.reject("The move leaves the king in check.");
        }
        return ValidationResult.ok();
    }

    private Piece pieceAt(GameState state, Position position) {
        return state == null || !state.getBoard().isInside(position)
                ? null : state.getBoard().getPiece(position);
    }

    private boolean isLegalCandidate(GameState state, Move candidate) {
        return checkCastling(state, candidate).valid()
                && checkKingSafety(state, candidate).valid();
    }
}
