package com.sameer.chess.game;

import com.sameer.chess.board.Board;
import com.sameer.chess.model.CastlingRights;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

final class GameTestPositions {
    private GameTestPositions() { }

    static GameState checkedPosition() {
        Board board = emptyKings();
        put(board, "e8", Color.BLACK, PieceType.ROOK);
        put(board, "a2", Color.WHITE, PieceType.ROOK);
        return state(board, Color.WHITE, noRights(), null, 0);
    }

    static Board emptyKings() {
        Board board = Board.empty();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "h8", Color.BLACK, PieceType.KING);
        return board;
    }

    static GameState state(Board board, Color active, CastlingRights rights,
            Position enPassantTarget, int halfmoveClock) {
        return new GameState(board, active, rights, enPassantTarget,
                halfmoveClock, 1, GameStatus.ACTIVE);
    }

    static CastlingRights noRights() {
        return new CastlingRights(false, false, false, false);
    }

    static void put(Board board, String square, Color color, PieceType type) {
        board.setPiece(Position.parse(square), new Piece(color, type));
    }
}
