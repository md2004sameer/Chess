package com.sameer.chess.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;
import com.sameer.chess.move.rule.RuleContext;

public final class GameState implements RuleContext {

    private Color sideToMove;
    private boolean whiteKingSideCastle;
    private boolean whiteQueenSideCastle;
    private boolean blackKingSideCastle;
    private boolean blackQueenSideCastle;
    private Position enPassantTarget;
    private int halfMoveClock;
    private int fullMoveNumber = 1;
    private final List<Move> moveHistory = new ArrayList<>();

    public GameState(Color sideToMove) {
        this.sideToMove = Objects.requireNonNull(sideToMove, "side to move must not be null");
    }

    private GameState(GameState source) {
        sideToMove = source.sideToMove;
        whiteKingSideCastle = source.whiteKingSideCastle;
        whiteQueenSideCastle = source.whiteQueenSideCastle;
        blackKingSideCastle = source.blackKingSideCastle;
        blackQueenSideCastle = source.blackQueenSideCastle;
        enPassantTarget = source.enPassantTarget;
        halfMoveClock = source.halfMoveClock;
        fullMoveNumber = source.fullMoveNumber;
        moveHistory.addAll(source.moveHistory);
    }

    public Color getSideToMove() {
        return sideToMove;
    }

    public int getHalfMoveClock() {
        return halfMoveClock;
    }

    public int getFullMoveNumber() {
        return fullMoveNumber;
    }

    public List<Move> getMoveHistory() {
        return List.copyOf(moveHistory);
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public boolean canCastle(Color color, boolean kingSide) {
        Objects.requireNonNull(color, "color must not be null");
        if (color == Color.WHITE) {
            return kingSide ? whiteKingSideCastle : whiteQueenSideCastle;
        }
        return kingSide ? blackKingSideCastle : blackQueenSideCastle;
    }

    public void setCastlingRights(Color color, boolean kingSide, boolean queenSide) {
        Objects.requireNonNull(color, "color must not be null");
        if (color == Color.WHITE) {
            whiteKingSideCastle = kingSide;
            whiteQueenSideCastle = queenSide;
        } else {
            blackKingSideCastle = kingSide;
            blackQueenSideCastle = queenSide;
        }
    }

    public boolean canCaptureEnPassant(Position from, Position to, Color color, Piece pawnAtTarget) {
        Objects.requireNonNull(from, "from position must not be null");
        Objects.requireNonNull(to, "to position must not be null");
        Objects.requireNonNull(color, "color must not be null");
        if (sideToMove != color
                || enPassantTarget == null
                || !enPassantTarget.equals(to)
                || to.getRow() != from.getRow() + color.forward()
                || Math.abs(from.getCol() - to.getCol()) != 1
                || pawnAtTarget == null) {
            return false;
        }
        return pawnAtTarget.getType() == Type.PAWN && pawnAtTarget.getColor() != color;
    }

    public GameState copy() {
        return new GameState(this);
    }

    public void restore(GameState snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        sideToMove = snapshot.sideToMove;
        whiteKingSideCastle = snapshot.whiteKingSideCastle;
        whiteQueenSideCastle = snapshot.whiteQueenSideCastle;
        blackKingSideCastle = snapshot.blackKingSideCastle;
        blackQueenSideCastle = snapshot.blackQueenSideCastle;
        enPassantTarget = snapshot.enPassantTarget;
        halfMoveClock = snapshot.halfMoveClock;
        fullMoveNumber = snapshot.fullMoveNumber;
        moveHistory.clear();
        moveHistory.addAll(snapshot.moveHistory);
    }

    public void recordMove(Move move, Piece movingPiece, Piece capturedPiece) {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(movingPiece, "moving piece must not be null");
        if (movingPiece.getColor() != sideToMove) {
            throw new IllegalArgumentException("Moving piece does not match side to move");
        }

        Color movingColor = sideToMove;
        revokeRights(move.from(), movingPiece);
        if (capturedPiece != null) {
            revokeRights(move.capturePosition(), capturedPiece);
        }

        enPassantTarget = null;
        if (movingPiece.getType() == Type.PAWN
                && move.from().getCol() == move.to().getCol()
                && move.from().getRow() == movingColor.pawnRow()
                && Math.abs(move.to().getRow() - move.from().getRow()) == 2) {
            enPassantTarget = new Position(
                    (move.from().getRow() + move.to().getRow()) / 2, move.from().getCol());
        }

        halfMoveClock = movingPiece.getType() == Type.PAWN || capturedPiece != null
                ? 0
                : halfMoveClock + 1;
        if (movingColor == Color.BLACK) {
            fullMoveNumber++;
        }
        sideToMove = movingColor.opposite();
        moveHistory.add(move);
    }

    private void revokeRights(Position position, Piece piece) {
        if (piece.getType() == Type.KING) {
            setRightsWithoutValidation(piece.getColor(), false, false);
            return;
        }
        if (piece.getType() != Type.ROOK) {
            return;
        }

        int homeRow = piece.getColor().homeRow();
        if (position.getRow() == homeRow && position.getCol() == 0) {
            setRightsWithoutValidation(piece.getColor(), canCastle(piece.getColor(), true), false);
        } else if (position.getRow() == homeRow && position.getCol() == 7) {
            setRightsWithoutValidation(piece.getColor(), false, canCastle(piece.getColor(), false));
        }
    }

    private void setRightsWithoutValidation(Color color, boolean kingSide, boolean queenSide) {
        if (color == Color.WHITE) {
            whiteKingSideCastle = kingSide;
            whiteQueenSideCastle = queenSide;
        } else {
            blackKingSideCastle = kingSide;
            blackQueenSideCastle = queenSide;
        }
    }
}
