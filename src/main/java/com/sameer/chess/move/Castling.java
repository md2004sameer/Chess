package com.sameer.chess.move;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Position;

public record Castling(boolean kingSide, int kingDestinationCol, int rookStartCol, int rookDestinationCol) {

    public static Castling forSide(boolean kingSide) {
        return kingSide
                ? new Castling(true, 6, 7, 5)
                : new Castling(false, 2, 0, 3);
    }

    public static Castling forMove(Move move) {
        return forSide(move.to().col() > move.from().col());
    }

    public Position kingDestination(Position kingFrom) {
        return new Position(kingFrom.row(), kingDestinationCol);
    }

    public Position rookStart(Position kingFrom) {
        return new Position(kingFrom.row(), rookStartCol);
    }

    public Position rookDestination(Position kingFrom) {
        return new Position(kingFrom.row(), rookDestinationCol);
    }

    public boolean hasClearPath(BoardReader board, Position kingFrom) {
        int step = kingSide ? 1 : -1;
        for (int col = kingFrom.col() + step; col != rookStartCol; col += step) {
            if (board.isOccupied(new Position(kingFrom.row(), col))) {
                return false;
            }
        }
        return true;
    }
}
