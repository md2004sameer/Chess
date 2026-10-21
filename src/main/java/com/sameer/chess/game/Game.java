package com.sameer.chess.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.sameer.chess.engine.AttackDetector;
import com.sameer.chess.engine.GameStatusEvaluator;
import com.sameer.chess.engine.MoveExecutor;
import com.sameer.chess.engine.MoveValidator;
import com.sameer.chess.board.Board;
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
    private final List<String> sanHistory = new ArrayList<>();
    private final Map<String, Integer> repetitionCounts = new HashMap<>();

    public Game() {
        this(GameState.initial());
    }

    /** Creates the standard initial chess game. */
    public static Game newGame() {
        return new Game();
    }

    public Game(GameState state) {
        this.state = Objects.requireNonNull(state, "state").copy();
        moveGenerator = new MoveGenerator();
        attackDetector = new AttackDetector();
        moveExecutor = new MoveExecutor();
        moveValidator = MoveValidator.standard(moveGenerator, moveExecutor, attackDetector);
        statusEvaluator = new GameStatusEvaluator(moveValidator, attackDetector);
        repetitionCounts.put(positionKey(), 1);
        this.state.setStatus(statusEvaluator.evaluate(this.state, 1));
    }

    /** Returns a snapshot that callers can inspect without mutating this game. */
    public GameState getState() {
        return state.copy();
    }

    public List<Move> getHistory() {
        return List.copyOf(history);
    }

    public List<String> getSanHistory() {
        return List.copyOf(sanHistory);
    }

    public List<Move> getLegalMoves() {
        return isOver() ? List.of() : moveValidator.generateLegalMoves(state);
    }

    /** Returns all legal moves for the side to move. */
    public List<Move> legalMoves() {
        return getLegalMoves();
    }

    /** Returns an independent board copy so callers cannot mutate the active game board. */
    public Board board() {
        return state.getBoard().copy();
    }

    public boolean isOver() {
        return state.getStatus().isTerminal();
    }

    /** Returns the status of the current position. */
    public GameStatus status() {
        return state.getStatus();
    }

    /** Returns the color whose turn it is. */
    public Color activeColor() {
        return state.getActiveColor();
    }

    /** Validates and plays a move, preserving the existing playMove API. */
    public MoveResult play(Move move) {
        return playMove(move);
    }

    public MoveResult playMove(Move move) {
        if (isOver()) return new MoveResult(false, "The game has already ended.", state.getStatus());
        if (move == null) return new MoveResult(false, "Move cannot be null.", state.getStatus());
        ValidationResult validation = moveValidator.validate(state, move);
        if (!validation.valid()) {
            return new MoveResult(false, validation.message(), state.getStatus());
        }

        String san = sanFor(move);
        moveExecutor.execute(state, move);
        history.add(move);
        String positionKey = positionKey();
        Integer previousRepetitions = repetitionCounts.get(positionKey);
        int repetitions = previousRepetitions == null ? 1 : previousRepetitions + 1;
        repetitionCounts.put(positionKey, repetitions);
        state.setStatus(statusEvaluator.evaluate(state, repetitions));
        if (state.getStatus() == GameStatus.CHECK) san += "+";
        if (state.getStatus() == GameStatus.CHECKMATE) san += "#";
        sanHistory.add(san);
        return new MoveResult(true, "Move accepted.", state.getStatus());
    }

    private String sanFor(Move move) {
        var board = state.getBoard();
        var piece = board.getPiece(move.from());
        boolean castling = piece.type() == PieceType.KING
                && Math.abs(move.to().col() - move.from().col()) == 2;
        if (castling) return move.to().col() > move.from().col() ? "O-O" : "O-O-O";

        boolean capture = board.getPiece(move.to()) != null
                || piece.type() == PieceType.PAWN
                        && move.from().col() != move.to().col()
                        && move.to().equals(state.getEnPassantTarget());
        StringBuilder san = new StringBuilder();
        if (piece.type() == PieceType.PAWN) {
            if (capture) san.append((char) ('a' + move.from().col()));
        } else {
            san.append(piece.type().symbol());
            appendDisambiguation(san, move, piece.color(), piece.type());
        }
        if (capture) san.append('x');
        san.append(move.to());
        if (move.promotionType() != null) {
            san.append('=').append(move.promotionType().symbol());
        }
        return san.toString();
    }

    private void appendDisambiguation(StringBuilder san, Move move, Color color, PieceType type) {
        boolean hasAlternative = false;
        boolean sameFile = false;
        boolean sameRank = false;
        for (Move candidate : moveValidator.generateLegalMoves(state)) {
            if (candidate.equals(move) || !candidate.to().equals(move.to())) continue;
            var other = state.getBoard().getPiece(candidate.from());
            if (other == null || other.color() != color || other.type() != type) continue;
            hasAlternative = true;
            if (candidate.from().col() == move.from().col()) sameFile = true;
            if (candidate.from().row() == move.from().row()) sameRank = true;
        }
        if (!hasAlternative) return;
        if (!sameFile) {
            san.append((char) ('a' + move.from().col()));
        } else if (!sameRank) {
            san.append((char) ('8' - move.from().row()));
        } else {
            san.append((char) ('a' + move.from().col()))
                    .append((char) ('8' - move.from().row()));
        }
    }

    public boolean canClaimFiftyMoveDraw() {
        return !isOver() && fiftyMoveRule.canClaim(state, currentRepetitionCount());
    }

    public boolean canClaimThreefoldRepetitionDraw() {
        return !isOver() && threefoldRule.canClaim(state, currentRepetitionCount());
    }

    public boolean canClaimDraw() {
        return canClaimFiftyMoveDraw() || canClaimThreefoldRepetitionDraw();
    }

    public boolean claimDraw() {
        if (canClaimFiftyMoveDraw()) return claimFiftyMoveDraw();
        return claimThreefoldRepetitionDraw();
    }

    /** Ends an active game by resignation; the active side is the resigning player. */
    public boolean resign() {
        if (isOver()) return false;
        state.setStatus(GameStatus.RESIGNED);
        return true;
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
