package com.sameer.chess.move.generator;

import java.util.List;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Position;
import com.sameer.chess.move.Move;

/** Generates pseudo-legal moves without modifying the board; results are never null. */
public interface MoveGenerator {

    List<Move> generateMoves(Position from, BoardReader board);
}
