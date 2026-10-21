package com.sameer.chess.engine;

import java.util.ArrayList;
import java.util.List;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

/** Evaluates check, mate, stalemate, and automatic draw status for a position. */
public final class GameStatusEvaluator {
    private final MoveValidator validator;
    private final AttackDetector attackDetector;

    public GameStatusEvaluator(MoveValidator validator, AttackDetector attackDetector) {
        this.validator = validator;
        this.attackDetector = attackDetector;
    }

    public GameStatus evaluate(GameState state, int repetitionCount) {
        boolean hasLegalMove = validator.hasAnyLegalMove(state);
        boolean inCheck = attackDetector.isInCheck(state.getBoard(), state.getActiveColor());

        if (!hasLegalMove) return inCheck ? GameStatus.CHECKMATE : GameStatus.STALEMATE;
        if (hasInsufficientMaterial(state.getBoard())) {
            return GameStatus.DRAW_INSUFFICIENT_MATERIAL;
        }
        if (state.getHalfmoveClock() >= 150) return GameStatus.DRAW_SEVENTY_FIVE_MOVE;
        if (repetitionCount >= 5) return GameStatus.DRAW_FIVEFOLD_REPETITION;
        return inCheck ? GameStatus.CHECK : GameStatus.ACTIVE;
    }

    private boolean hasInsufficientMaterial(Board board) {
        List<Position> bishops = new ArrayList<>();
        int knights = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                if (piece == null || piece.type() == PieceType.KING) continue;
                switch (piece.type()) {
                    case PAWN, ROOK, QUEEN -> { return false; }
                    case KNIGHT -> knights++;
                    case BISHOP -> bishops.add(new Position(row, col));
                    default -> { }
                }
            }
        }

        int minors = knights + bishops.size();
        if (minors <= 1) return true;
        if (knights > 0) return false;
        boolean firstSquareColor = (bishops.getFirst().row() + bishops.getFirst().col()) % 2 == 0;
        for (Position bishop : bishops) {
            if (((bishop.row() + bishop.col()) % 2 == 0) != firstSquareColor) return false;
        }
        return true;
    }
}
