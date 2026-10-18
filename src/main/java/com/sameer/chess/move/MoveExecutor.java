package com.sameer.chess.move;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sameer.chess.board.MutableBoard;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Type;

@Component
public class MoveExecutor {

    public record Executed(Move move, Piece moved, Piece captured) {
        public Executed {
            Objects.requireNonNull(move, "move must not be null");
            Objects.requireNonNull(moved, "moved piece must not be null");
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
        return new Executed(move, movingPiece, capturedPiece);
    }

    public void undo(MutableBoard board, Executed executed) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(executed, "executed move must not be null");

        Move move = executed.move();
        Piece expectedPiece = move.kind() == MoveKind.PROMOTION
                ? new Piece(executed.moved().getColor(), move.promotionType())
                : executed.moved();
        Piece movedPiece = board.getPiece(move.to());
        if (!expectedPiece.equals(movedPiece)) {
            throw new IllegalStateException("Expected moved piece at destination");
        }
        if (board.isOccupied(move.from())) {
            throw new IllegalStateException("Origin square occupied");
        }

        Castling castling = null;
        if (move.kind() == MoveKind.CASTLING) {
            castling = Castling.forMove(move);
            if (board.isOccupied(castling.rookStart(move.from()))) {
                throw new IllegalStateException("Castling rook start square occupied");
            }
            Piece rook = board.getPiece(castling.rookDestination(move.from()));
            if (rook == null || rook.getColor() != executed.moved().getColor() || rook.getType() != Type.ROOK) {
                throw new IllegalStateException("Expected castling rook at destination");
            }
        }

        Position capturedPosition = move.kind() == MoveKind.EN_PASSANT
                ? new Position(move.from().row(), move.to().col())
                : move.to();
        if (move.isEnPassant() && board.isOccupied(capturedPosition)) {
            throw new IllegalStateException("Captured piece square occupied");
        }

        board.removePiece(move.to());
        if (move.kind() == MoveKind.CASTLING) {
            board.movePiece(castling.rookDestination(move.from()), castling.rookStart(move.from()));
        }
        board.placePiece(move.from(), executed.moved());

        if (executed.captured() != null) {
            board.placePiece(capturedPosition, executed.captured());
        }
    }
}
