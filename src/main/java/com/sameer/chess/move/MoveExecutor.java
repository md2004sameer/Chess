package com.sameer.chess.move;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.board.BoardWriter;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;

@Component
public class MoveExecutor {

    public Move execute(BoardReader board, BoardWriter writer, Move move, GameState gameState) {
        Objects.requireNonNull(board, "board must not be null");
        Objects.requireNonNull(writer, "board writer must not be null");
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(gameState, "game state must not be null");

        Piece movingPiece = board.getPiece(move.from());
        if (movingPiece == null) {
            throw new IllegalArgumentException("No piece at the source position");
        }

        Position capturePosition = move.to();
        Piece capturedPiece;
        if (move.kind() == MoveKind.EN_PASSANT) {
            validateEnPassant(board, move, movingPiece, gameState);
            capturePosition = new Position(move.from().row(), move.to().col());
            capturedPiece = board.getPiece(capturePosition);
        } else {
            capturedPiece = board.getPiece(move.to());
            if (capturedPiece != null
                    && (capturedPiece.getColor() == movingPiece.getColor()
                            || capturedPiece.getType() == Type.KING)) {
                throw new IllegalArgumentException("Move cannot capture that piece");
            }
        }

        validatePromotion(move, movingPiece);
        if (move.kind() == MoveKind.CASTLING) {
            validateCastling(board, move, movingPiece, gameState);
        }

        if (capturedPiece != null) {
            writer.removePiece(capturePosition);
        }
        if (move.kind() == MoveKind.CASTLING) {
            boolean kingSide = move.to().col() > move.from().col();
            int rookFromCol = kingSide ? 7 : 0;
            int rookToCol = kingSide ? 5 : 3;
            writer.movePiece(move.from(), move.to());
            writer.movePiece(new Position(move.from().row(), rookFromCol),
                    new Position(move.from().row(), rookToCol));
        } else if (move.kind() == MoveKind.PROMOTION) {
            writer.removePiece(move.from());
            writer.placePiece(move.to(), new Piece(movingPiece.getColor(), move.promotionType()));
        } else {
            writer.movePiece(move.from(), move.to());
        }
        return capturedPiece == null ? move : move.withCapturedPiece(capturedPiece);
    }

    public void undo(BoardWriter writer, Move move, Piece originalPiece) {
        Objects.requireNonNull(writer, "board writer must not be null");
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(originalPiece, "original piece must not be null");

        writer.removePiece(move.to());
        if (move.kind() == MoveKind.CASTLING) {
            boolean kingSide = move.to().col() > move.from().col();
            int rookFromCol = kingSide ? 7 : 0;
            int rookToCol = kingSide ? 5 : 3;
            writer.movePiece(new Position(move.from().row(), rookToCol),
                    new Position(move.from().row(), rookFromCol));
            writer.placePiece(move.from(), originalPiece);
        } else {
            writer.placePiece(move.from(), originalPiece);
        }

        if (move.capturedPiece() != null) {
            Position capturedPosition = move.kind() == MoveKind.EN_PASSANT
                    ? new Position(move.from().row(), move.to().col())
                    : move.to();
            writer.placePiece(capturedPosition, move.capturedPiece());
        }
    }

    private void validateEnPassant(
            BoardReader board, Move move, Piece movingPiece, GameState gameState) {
        Piece capturedPawn = board.getPiece(new Position(move.from().row(), move.to().col()));
        if (movingPiece.getType() != Type.PAWN
                || board.isOccupied(move.to())
                || !gameState.canCaptureEnPassant(
                        move.from(), move.to(), movingPiece.getColor(), capturedPawn)) {
            throw new IllegalArgumentException("Invalid en-passant move");
        }
    }

    private void validatePromotion(Move move, Piece movingPiece) {
        boolean reachesPromotionRank = movingPiece.getType() == Type.PAWN
                && move.to().row() == (movingPiece.getColor() == Color.WHITE ? 0 : 7);
        if (reachesPromotionRank != (move.kind() == MoveKind.PROMOTION)) {
            throw new IllegalArgumentException("A pawn reaching its final rank must promote");
        }
        if (move.kind() == MoveKind.PROMOTION && movingPiece.getType() != Type.PAWN) {
            throw new IllegalArgumentException("Only a pawn can promote");
        }
    }

    private void validateCastling(
            BoardReader board, Move move, Piece king, GameState gameState) {
        int homeRow = king.getColor() == Color.WHITE ? 7 : 0;
        boolean kingSide = move.to().col() == 6;
        if (king.getType() != Type.KING
                || move.from().row() != homeRow
                || move.from().col() != 4
                || move.to().row() != homeRow
                || (!kingSide && move.to().col() != 2)
                || !gameState.canCastle(king.getColor(), kingSide)
                || board.isOccupied(move.to())) {
            throw new IllegalArgumentException("Invalid castling move");
        }
        int rookCol = kingSide ? 7 : 0;
        Piece rook = board.getPiece(new Position(homeRow, rookCol));
        if (rook == null || rook.getType() != Type.ROOK || rook.getColor() != king.getColor()) {
            throw new IllegalArgumentException("Castling rook is not on its home square");
        }
        int step = kingSide ? 1 : -1;
        for (int col = move.from().col() + step; col != rookCol; col += step) {
            if (board.isOccupied(new Position(homeRow, col))) {
                throw new IllegalArgumentException("Squares between king and rook must be empty");
            }
        }
    }
}
