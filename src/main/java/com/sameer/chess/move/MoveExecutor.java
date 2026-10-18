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

        Piece capturedPiece = board.getPiece(move.capturePosition());
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
        return switch (move.kind()) {
            case CASTLING -> castle(board, move, movingPiece, capturedPiece);
            case PROMOTION -> promote(board, move, movingPiece, capturedPiece);
            case NORMAL, EN_PASSANT -> standard(board, move, movingPiece, capturedPiece);
        };
    }

    public void undo(MutableBoard board, Executed executed) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(executed, "executed move must not be null");

        switch (executed.move().kind()) {
            case CASTLING -> uncastle(board, executed);
            case PROMOTION -> unpromote(board, executed);
            case NORMAL, EN_PASSANT -> unmove(board, executed);
        }
    }

    private Executed castle(
            MutableBoard board, Move move, Piece movingPiece, Piece capturedPiece) {
        Castling castling = Castling.forMove(move);
        board.movePiece(move.from(), move.to());
        board.movePiece(castling.rookStart(move.from()), castling.rookDestination(move.from()));
        return new Executed(move, movingPiece, capturedPiece);
    }

    private Executed promote(
            MutableBoard board, Move move, Piece movingPiece, Piece capturedPiece) {
        if (capturedPiece != null) {
            board.removePiece(move.capturePosition());
        }
        board.removePiece(move.from());
        board.placePiece(move.to(), new Piece(movingPiece.getColor(), move.promotionType()));
        return new Executed(move, movingPiece, capturedPiece);
    }

    private Executed standard(
            MutableBoard board, Move move, Piece movingPiece, Piece capturedPiece) {
        if (capturedPiece != null) {
            board.removePiece(move.capturePosition());
        }
        board.movePiece(move.from(), move.to());
        return new Executed(move, movingPiece, capturedPiece);
    }

    private void uncastle(MutableBoard board, Executed executed) {
        Move move = executed.move();
        validateMovedPiece(board, move.to(), executed.moved());
        validateOriginIsEmpty(board, move.from());

        Castling castling = Castling.forMove(move);
        if (board.isOccupied(castling.rookStart(move.from()))) {
            throw new IllegalStateException("Castling rook start square occupied");
        }
        Piece rook = board.getPiece(castling.rookDestination(move.from()));
        if (rook == null || rook.getColor() != executed.moved().getColor() || rook.getType() != Type.ROOK) {
            throw new IllegalStateException("Expected castling rook at destination");
        }

        board.removePiece(move.to());
        board.movePiece(castling.rookDestination(move.from()), castling.rookStart(move.from()));
        board.placePiece(move.from(), executed.moved());
    }

    private void unpromote(MutableBoard board, Executed executed) {
        Move move = executed.move();
        Piece promotedPiece = new Piece(executed.moved().getColor(), move.promotionType());
        validateMovedPiece(board, move.to(), promotedPiece);
        validateOriginIsEmpty(board, move.from());

        board.removePiece(move.to());
        board.placePiece(move.from(), executed.moved());
        restoreCaptured(board, executed);
    }

    private void unmove(MutableBoard board, Executed executed) {
        Move move = executed.move();
        validateMovedPiece(board, move.to(), executed.moved());
        validateOriginIsEmpty(board, move.from());
        if (move.isEnPassant() && board.isOccupied(move.capturePosition())) {
            throw new IllegalStateException("Captured piece square occupied");
        }

        board.removePiece(move.to());
        board.placePiece(move.from(), executed.moved());
        restoreCaptured(board, executed);
    }

    private void validateMovedPiece(MutableBoard board, Position position, Piece expectedPiece) {
        if (!expectedPiece.equals(board.getPiece(position))) {
            throw new IllegalStateException("Expected moved piece at destination");
        }
    }

    private void validateOriginIsEmpty(MutableBoard board, Position position) {
        if (board.isOccupied(position)) {
            throw new IllegalStateException("Origin square occupied");
        }
    }

    private void restoreCaptured(MutableBoard board, Executed executed) {
        if (executed.captured() != null) {
            board.placePiece(executed.move().capturePosition(), executed.captured());
        }
    }
}
