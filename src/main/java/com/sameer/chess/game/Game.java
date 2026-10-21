package com.sameer.chess.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.sameer.chess.board.Board;
import com.sameer.chess.engine.AttackDetector;
import com.sameer.chess.engine.GameStatusEvaluator;
import com.sameer.chess.engine.MoveExecutor;
import com.sameer.chess.engine.MoveValidator;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.ValidationResult;
import com.sameer.chess.movegen.MoveGenerator;

/** Coordinates legal moves, game history, repetition, and status evaluation. */
public final class Game {
    private final GameState state;
    private final MoveGenerator moveGenerator;
    private final AttackDetector attackDetector;
    private final MoveExecutor moveExecutor;
    private final MoveValidator moveValidator;
    private final GameStatusEvaluator statusEvaluator;
    private final FiftyMoveRule fiftyMoveRule = new FiftyMoveRule();
    private final ThreefoldRepetitionRule threefoldRule = new ThreefoldRepetitionRule();
    private final List<Move> history = new ArrayList<>();
    private final Map<String, Integer> repetitionCounts = new HashMap<>();

    public Game() {
        this(GameState.initial());
    }

    public Game(GameState state) {
        this.state = Objects.requireNonNull(state, "state");
        moveGenerator = new MoveGenerator();
        attackDetector = new AttackDetector();
        moveExecutor = new MoveExecutor();
        moveValidator = MoveValidator.standard(moveGenerator, moveExecutor, attackDetector);
        statusEvaluator = new GameStatusEvaluator(moveValidator, attackDetector);
        repetitionCounts.put(positionKey(), 1);
        state.setStatus(statusEvaluator.evaluate(state, 1));
    }

    public GameState getState() {
        return state;
    }

    public List<Move> getHistory() {
        return List.copyOf(history);
    }

    public List<Move> getLegalMoves() {
        return isOver() ? List.of() : moveValidator.generateLegalMoves(state);
    }

    public boolean isOver() {
        return state.getStatus().isTerminal();
    }

    public MoveResult playMove(Move move) {
        if (isOver()) return new MoveResult(false, "The game has already ended.", state.getStatus());
        if (move == null) return new MoveResult(false, "Move cannot be null.", state.getStatus());
        ValidationResult validation = moveValidator.validate(state, move);
        if (!validation.valid()) {
            return new MoveResult(false, validation.message(), state.getStatus());
        }

        moveExecutor.execute(state, move);
        history.add(move);
        int repetitions = repetitionCounts.merge(positionKey(), 1, Integer::sum);
        state.setStatus(statusEvaluator.evaluate(state, repetitions));
        return new MoveResult(true, "Move accepted.", state.getStatus());
    }

    public boolean canClaimFiftyMoveDraw() {
        return !isOver() && fiftyMoveRule.canClaim(state, currentRepetitionCount());
    }

    public boolean canClaimThreefoldRepetitionDraw() {
        return !isOver() && threefoldRule.canClaim(state, currentRepetitionCount());
    }

    public boolean claimFiftyMoveDraw() {
        if (!canClaimFiftyMoveDraw()) return false;
        state.setStatus(GameStatus.DRAW_FIFTY_MOVE);
        return true;
    }

    public boolean claimThreefoldRepetitionDraw() {
        if (!canClaimThreefoldRepetitionDraw()) return false;
        state.setStatus(GameStatus.DRAW_THREEFOLD_REPETITION);
        return true;
    }

    private int currentRepetitionCount() {
        return repetitionCounts.getOrDefault(positionKey(), 1);
    }

    private String positionKey() {
        Position enPassantTarget = state.getEnPassantTarget();
        if (enPassantTarget == null) return state.positionKey(false);
        boolean legalEnPassantAvailable = moveValidator.generateLegalMoves(state).stream()
                .anyMatch(move -> move.to().equals(enPassantTarget)
                        && state.getBoard().getPiece(move.from()).type() == PieceType.PAWN
                        && move.from().col() != move.to().col());
        return state.positionKey(legalEnPassantAvailable);
    }
}
