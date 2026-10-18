package com.sameer.chess.board;

import java.util.List;

import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;

/** Reads piece placement without mutating the board. */
public interface BoardReader {

    Piece getPiece(Position position);

    boolean isOccupied(Position position);

    boolean isInBounds(int row, int col);

    List<Position> getOccupiedPositions();

    Position findKing(Color color);
}
