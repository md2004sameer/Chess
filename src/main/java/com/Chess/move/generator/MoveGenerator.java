package com.Chess.move.generator;

import java.util.List;

import com.Chess.board.Board;
import com.Chess.model.Piece;
import com.Chess.move.Move;

public interface MoveGenerator {

    List<Move> generateMoves(Piece piece, Board board);
}
