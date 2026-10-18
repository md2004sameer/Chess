package com.sameer.chess.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.board.MutableBoard;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;
import com.sameer.chess.move.MoveExecutor;
import com.sameer.chess.move.MoveExecutor.Executed;
import com.sameer.chess.move.MoveKind;
import com.sameer.chess.move.generator.MoveGeneratorRegistry;
import com.sameer.chess.move.rule.SpecialMoveRule;

@Component
public class MoveValidator {

    private final MoveGeneratorRegistry generators;
    private final List<SpecialMoveRule> specialMoveRules;
    private final AttackDetector attackDetector;
    private final MoveExecutor moveExecutor;

    public MoveValidator(
            MoveGeneratorRegistry generators,
            List<SpecialMoveRule> specialMoveRules,
            AttackDetector attackDetector,
            MoveExecutor moveExecutor) {
        this.generators = Objects.requireNonNull(generators, "generators must not be null");
        this.specialMoveRules = List.copyOf(
                Objects.requireNonNull(specialMoveRules, "special move rules must not be null"));
        this.attackDetector = Objects.requireNonNull(attackDetector, "attack detector must not be null");
        this.moveExecutor = Objects.requireNonNull(moveExecutor, "move executor must not be null");
    }

    public List<Move> getLegalMoves(
            MutableBoard board, Position from, GameState gameState) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(from, "from position must not be null");
        Objects.requireNonNull(gameState, "game state must not be null");
        Color playerColor = gameState.getSideToMove();

        Piece piece = board.getPiece(from);
        if (piece == null || piece.getColor() != playerColor || board.findKing(playerColor) == null) {
            return List.of();
        }
        return pseudoLegalMoves(board, from, piece, gameState).stream()
                .filter(move -> isLegalMove(board, move, gameState))
                .toList();
    }

    public boolean isLegalMove(
            MutableBoard board, Move move, GameState gameState) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(gameState, "game state must not be null");
        Color playerColor = gameState.getSideToMove();

        Piece movingPiece = board.getPiece(move.from());
        if (movingPiece == null
                || movingPiece.getColor() != playerColor
                || move.from().equals(move.to())
                || board.findKing(playerColor) == null) {
            return false;
        }
        Piece targetPiece = board.getPiece(move.to());
        if (targetPiece != null && targetPiece.getType() == Type.KING) {
            return false;
        }

        boolean isCandidate = pseudoLegalMoves(board, move.from(), movingPiece, gameState).stream()
                .anyMatch(candidate -> sameMoveChoice(candidate, move));
        if (!isCandidate) {
            return false;
        }

        Color opponentColor = opponentOf(playerColor);
        if (move.kind() == MoveKind.CASTLING) {
            if (attackDetector.isSquareAttacked(board, move.from(), opponentColor)) {
                return false;
            }
            int step = Integer.compare(move.to().col(), move.from().col());
            Position transit = new Position(move.from().row(), move.from().col() + step);
            Move transitMove = new Move(move.from(), transit);
            Executed executedTransit = moveExecutor.execute(board, transitMove);
            try {
                if (attackDetector.isSquareAttacked(board, transit, opponentColor)) {
                    return false;
                }
            } finally {
                moveExecutor.undo(board, executedTransit);
            }
        }

        Executed executedMove = moveExecutor.execute(board, move);
        try {
            Position king = board.findKing(playerColor);
            return king != null && !attackDetector.isSquareAttacked(board, king, opponentColor);
        } finally {
            moveExecutor.undo(board, executedMove);
        }
    }

    private List<Move> pseudoLegalMoves(
            BoardReader board, Position from, Piece piece, GameState gameState) {
        List<Move> candidates = new ArrayList<>(
                generators.getGenerator(piece.getType()).generateMoves(from, board));
        for (SpecialMoveRule rule : specialMoveRules) {
            candidates = new ArrayList<>(rule.apply(piece, from, candidates, board, gameState));
        }
        return List.copyOf(candidates);
    }

    private boolean sameMoveChoice(Move candidate, Move requested) {
        return candidate.to().equals(requested.to())
                && candidate.promotionType() == requested.promotionType()
                && candidate.kind() == requested.kind();
    }

    private Color opponentOf(Color color) {
        return color.opposite();
    }
}
