package com.sameer.chess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

class GameDrawRuleTests {
    @Test
    void failedFiftyMoveClaimLeavesTheCurrentGameUnchanged() {
        Game game = new Game();
        String before = boardKey(game.board());

        assertFalse(game.canClaimFiftyMoveDraw());
        assertFalse(game.claimFiftyMoveDraw());

        assertEquals(before, boardKey(game.board()));
        assertEquals(GameStatus.ACTIVE, game.getState().getStatus());
        assertEquals(Color.WHITE, game.getState().getActiveColor());
        assertTrue(game.getHistory().isEmpty());
    }

    @Test
    void quietMoveReachesFiftyMoveClaimThreshold() {
        Game game = gameAtHalfmoveClock(99);

        assertTrue(game.playMove(Move.parse("a1a2")).accepted());

        assertEquals(100, game.getState().getHalfmoveClock());
        assertTrue(game.canClaimFiftyMoveDraw());
        assertTrue(game.claimFiftyMoveDraw());
        assertEquals(GameStatus.DRAW_FIFTY_MOVE, game.getState().getStatus());
    }

    @Test
    void pawnMoveAndCaptureResetTheHalfmoveClock() {
        Game pawnGame = gameAtHalfmoveClock(99);

        assertTrue(pawnGame.playMove(Move.parse("b2b3")).accepted());
        assertEquals(0, pawnGame.getState().getHalfmoveClock());

        Board board = GameTestPositions.emptyKings();
        GameTestPositions.put(board, "a1", Color.WHITE, PieceType.ROOK);
        GameTestPositions.put(board, "a8", Color.BLACK, PieceType.BISHOP);
        Game captureGame = new Game(GameTestPositions.state(board, Color.WHITE,
                GameTestPositions.noRights(), null, 99));

        assertTrue(captureGame.playMove(Move.parse("a1a8")).accepted());
        assertEquals(0, captureGame.getState().getHalfmoveClock());
    }

    @Test
    void quietMoveAtSeventyFiveMoveThresholdEndsTheGameAndRejectsFurtherMoves() {
        Game game = gameAtHalfmoveClock(149);

        assertTrue(game.playMove(Move.parse("a1a2")).accepted());

        assertEquals(GameStatus.DRAW_SEVENTY_FIVE_MOVE, game.getState().getStatus());
        assertTrue(game.isOver());
        assertFalse(game.playMove(Move.parse("h8g8")).accepted());
        assertEquals(1, game.getHistory().size());
    }

    private Game gameAtHalfmoveClock(int halfmoveClock) {
        Board board = GameTestPositions.emptyKings();
        GameTestPositions.put(board, "a1", Color.WHITE, PieceType.ROOK);
        GameTestPositions.put(board, "h7", Color.BLACK, PieceType.ROOK);
        GameTestPositions.put(board, "b2", Color.WHITE, PieceType.PAWN);
        return new Game(GameTestPositions.state(board, Color.WHITE,
                GameTestPositions.noRights(), null, halfmoveClock));
    }

    private String boardKey(Board board) {
        StringBuilder key = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                var piece = board.getPiece(new Position(row, col));
                key.append(piece == null ? '.' : piece.displaySymbol());
            }
        }
        return key.toString();
    }
}
