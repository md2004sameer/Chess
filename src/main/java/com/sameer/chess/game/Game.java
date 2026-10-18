package com.sameer.chess.game;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.board.MutableBoard;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.move.Move;
import com.sameer.chess.move.MoveExecutor;
import com.sameer.chess.move.MoveExecutor.Executed;
import com.sameer.chess.validation.MoveValidator;

public final class Game {

    private final MutableBoard board;
    private final GameState state;
    private final MoveValidator moveValidator;
    private final MoveExecutor moveExecutor;
    private final Deque<HistoryEntry> history = new ArrayDeque<>();

    public Game(
            MutableBoard board,
            GameState state,
            MoveValidator moveValidator,
            MoveExecutor moveExecutor) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.state = Objects.requireNonNull(state, "game state must not be null");
        this.moveValidator = Objects.requireNonNull(moveValidator, "move validator must not be null");
        this.moveExecutor = Objects.requireNonNull(moveExecutor, "move executor must not be null");
    }

    public BoardReader getBoard() {
        return board;
    }

    public GameState getState() {
        return state;
    }

    public List<Move> getLegalMoves(Position from) {
        return moveValidator.getLegalMoves(board, from, state);
    }

    public Move playMove(Move move) {
        Objects.requireNonNull(move, "move must not be null");
        if (!moveValidator.isLegalMove(board, move, state)) {
            throw new IllegalArgumentException("Move is not legal in the current game state");
        }

        Piece movingPiece = board.getPiece(move.from());
        GameState previousState = state.copy();
        Executed executed = moveExecutor.execute(board, move);
        state.recordMove(executed.move(), movingPiece, executed.captured());
        history.push(new HistoryEntry(executed, previousState));
        return executed.move();
    }

    public void undoMove() {
        if (history.isEmpty()) {
            throw new IllegalStateException("There is no game move to undo");
        }
        HistoryEntry previous = history.peek();
        moveExecutor.undo(board, previous.executed());
        state.restore(previous.previousState());
        history.pop();
    }

    private record HistoryEntry(Executed executed, GameState previousState) {
    }
}
