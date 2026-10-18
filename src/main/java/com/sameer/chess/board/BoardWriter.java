package com.sameer.chess.board;

import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;

/** Mutates piece placement; game rules are handled by the move pipeline. */
public interface BoardWriter {

    void placePiece(Position position, Piece piece);

    Piece removePiece(Position position);

    void movePiece(Position from, Position to);
}
