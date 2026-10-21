package com.sameer.chess.movegen;

import java.util.HashSet;
import java.util.Set;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.CastlingRights;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

final class GeneratorTestSupport {
    private GeneratorTestSupport() {
    }

    static Board boardWithKings(String whiteKing, String blackKing) {
        Board board = Board.empty();
        put(board, whiteKing, Color.WHITE, PieceType.KING);
        put(board, blackKing, Color.BLACK, PieceType.KING);
        return board;
    }

    static GameState state(Board board) {
        return new GameState(board, Color.WHITE, noCastling(), null, 0, 1, GameStatus.ACTIVE);
    }

    static GameState state(Board board, CastlingRights rights, Position enPassantTarget) {
        return new GameState(board, Color.WHITE, rights, enPassantTarget, 0, 1, GameStatus.ACTIVE);
    }

    static CastlingRights noCastling() {
        return new CastlingRights(false, false, false, false);
    }

    static void put(Board board, String square, Color color, PieceType type) {
        board.setPiece(Position.parse(square), new Piece(color, type));
    }

    static Set<String> movesFrom(PieceMoveGenerator generator, GameState state, String from) {
        Set<String> result = new HashSet<>();
        for (var move : generator.generate(state, Position.parse(from))) result.add(move.toString());
        return result;
    }
}
