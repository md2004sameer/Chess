package com.sameer.chess.move;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sameer.chess.board.MutableBoard;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Type;

@Component
public class MoveExecutor {

    public record Executed(Move move, Piece captured) {
        public Executed {
            Objects.requireNonNull(move, "move must not be null");
        }
    }

    public Executed execute(MutableBoard board, Move move) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(move, "move must not be null");

        Piece movingPiece = board.getPiece(move.from());
        if (movingPiece == null) {
            throw new IllegalArgumentException("No piece at the source position");
        }

        Position capturePosition = move.kind() == MoveKind.EN_PASSANT
                ? new Position(move.from().row(), move.to().col())
                : move.to();
        Piece capturedPiece = board.getPiece(capturePosition);
        if (move.from().equals(move.to())) {
            throw new IllegalArgumentException("from equals to");
        }
        if (capturedPiece != null && (capturedPiece.getColor() == movingPiece.getColor()
                || capturedPiece.getType() == Type.KING)) {
            throw new IllegalArgumentException("Illegal capture");
        }
        if (move.isEnPassant() && capturedPiece == null) {
            throw new IllegalArgumentException("No pawn to capture");
        }
        if (move.kind() == MoveKind.CASTLING) {
            Castling castling = Castling.forMove(move);
            board.movePiece(move.from(), move.to());
            board.movePiece(castling.rookStart(move.from()), castling.rookDestination(move.from()));
        } else if (move.kind() == MoveKind.PROMOTION) {
            if (capturedPiece != null) {
                board.removePiece(capturePosition);
            }
            board.removePiece(move.from());
            board.placePiece(move.to(), new Piece(movingPiece.getColor(), move.promotionType()));
        } else {
            if (capturedPiece != null) {
                board.removePiece(capturePosition);
            }
            board.movePiece(move.from(), move.to());
        }
        return new Executed(move, capturedPiece);
    }

    public void undo(MutableBoard board, Executed executed, Piece originalPiece) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(executed, "executed move must not be null");
        Objects.requireNonNull(originalPiece, "original piece must not be null");

        Move move = executed.move();
        Piece movedPiece = board.removePiece(move.to());
        Piece expectedPiece = move.kind() == MoveKind.PROMOTION
                ? new Piece(originalPiece.getColor(), move.promotionType())
                : originalPiece;
        if (!expectedPiece.equals(movedPiece)) {
            if (movedPiece != null) {
                board.placePiece(move.to(), movedPiece);
            }
            throw new IllegalStateException("Expected moved piece at destination when undoing");
        }
        if (move.kind() == MoveKind.CASTLING) {
            Castling castling = Castling.forMove(move);
            board.movePiece(castling.rookDestination(move.from()), castling.rookStart(move.from()));
            board.placePiece(move.from(), originalPiece);
        } else {
            board.placePiece(move.from(), originalPiece);
        }

        if (executed.captured() != null) {
            Position capturedPosition = move.kind() == MoveKind.EN_PASSANT
                    ? new Position(move.from().row(), move.to().col())
                    : move.to();
            board.placePiece(capturedPosition, executed.captured());
        }
    }
}
