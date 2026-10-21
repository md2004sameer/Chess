package com.sameer.chess.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.CastlingRights;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

class CastlingBehaviorTests {
    @Test
    void castlingOutOfCheckIsRejected() {
        Board board = castlingBoard();
        put(board, "e8", Color.BLACK, PieceType.ROOK);
        Game game = game(board, Color.WHITE, fullRights());

        assertFalse(game.playMove(Move.parse("e1g1")).accepted());
    }

    @Test
    void castlingIntoAttackedSquareIsRejected() {
        Board board = castlingBoard();
        put(board, "g8", Color.BLACK, PieceType.ROOK);
        Game game = game(board, Color.WHITE, fullRights());

        assertFalse(game.playMove(Move.parse("e1g1")).accepted());
    }

    @Test
    void castlingAfterKingMovedAndReturnedIsRejected() {
        Game game = game(castlingBoard(), Color.WHITE, fullRights());
        play(game, "e1e2", "b8c8", "e2e1", "c8b8");

        assertFalse(game.playMove(Move.parse("e1g1")).accepted());
        assertFalse(game.getState().getCastlingRights().canCastle(Color.WHITE, true));
    }

    @Test
    void castlingAfterRookMovedAndReturnedIsRejected() {
        Game game = game(castlingBoard(), Color.WHITE, fullRights());
        play(game, "h1h2", "b8c8", "h2h1", "c8b8");

        assertFalse(game.playMove(Move.parse("e1g1")).accepted());
        assertFalse(game.getState().getCastlingRights().canCastle(Color.WHITE, true));
    }

    @Test
    void castlingWithPieceBetweenKingAndRookIsRejected() {
        Board board = castlingBoard();
        put(board, "f1", Color.WHITE, PieceType.BISHOP);
        Game game = game(board, Color.WHITE, fullRights());

        assertFalse(game.playMove(Move.parse("e1g1")).accepted());
    }

    @Test
    void castlingAfterRequiredRookWasCapturedIsRejected() {
        Board board = castlingBoard();
        put(board, "h8", Color.BLACK, PieceType.ROOK);
        Game game = game(board, Color.BLACK, fullRights());
        assertTrue(game.playMove(Move.parse("h8h1")).accepted());

        assertFalse(game.getState().getCastlingRights().canCastle(Color.WHITE, true));
        assertFalse(game.playMove(Move.parse("e1g1")).accepted());
    }

    @Test
    void queensideCastlingRightsRemainIndependent() {
        Game game = game(castlingBoard(), Color.WHITE, fullRights());

        assertTrue(game.getState().getCastlingRights().canCastle(Color.WHITE, false));
        assertTrue(game.playMove(Move.parse("e1c1")).accepted());
    }

    private Game game(Board board, Color active, CastlingRights rights) {
        GameState state = new GameState(board, active, rights, null, 0, 1, GameStatus.ACTIVE);
        return new Game(state);
    }

    private Board castlingBoard() {
        Board board = Board.empty();
        put(board, "e1", Color.WHITE, PieceType.KING);
        put(board, "b8", Color.BLACK, PieceType.KING);
        put(board, "a1", Color.WHITE, PieceType.ROOK);
        put(board, "h1", Color.WHITE, PieceType.ROOK);
        return board;
    }

    private CastlingRights fullRights() {
        return new CastlingRights(true, true, true, true);
    }

    private void play(Game game, String... moves) {
        for (String move : moves) assertTrue(game.playMove(Move.parse(move)).accepted(), move);
    }

    private void put(Board board, String square, Color color, PieceType type) {
        board.setPiece(Position.parse(square), new Piece(color, type));
    }
}
