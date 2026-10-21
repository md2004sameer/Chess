package com.sameer.chess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

class GameLifecycleTests {
    @Test
    void newGameStartsWithWhiteAndTwentyLegalMoves() {
        Game game = new Game();

        assertEquals(Color.WHITE, game.getState().getActiveColor());
        assertEquals(GameStatus.ACTIVE, game.getState().getStatus());
        assertTrue(game.getHistory().isEmpty());
        assertEquals(20, game.getLegalMoves().size());
        assertFalse(game.isOver());
    }

    @Test
    void legalMovesUpdateBoardHistoryPlayersAndClocks() {
        Game game = new Game();
        play(game, "e2e4", "e7e5", "g1f3");

        assertEquals(Color.BLACK, game.getState().getActiveColor());
        assertEquals(3, game.getHistory().size());
        assertEquals(List.of(Move.parse("e2e4"), Move.parse("e7e5"),
                Move.parse("g1f3")), game.getHistory());
        assertEquals(1, game.getState().getHalfmoveClock());
        assertEquals(2, game.getState().getFullmoveNumber());
        assertEquals(PieceType.KNIGHT, game.getState().getBoard()
                .getPiece(Position.parse("f3")).type());
        assertEquals(PieceType.PAWN, game.getState().getBoard()
                .getPiece(Position.parse("e4")).type());
        assertNull(game.getState().getEnPassantTarget());
    }

    @Test
    void nullMoveIsRejectedWithoutEndingGame() {
        Game game = new Game();

        MoveResult result = game.playMove(null);

        assertFalse(result.accepted());
        assertEquals("Move cannot be null.", result.message());
        assertEquals(GameStatus.ACTIVE, result.status());
        assertEquals(Color.WHITE, game.getState().getActiveColor());
        assertTrue(game.getHistory().isEmpty());
        assertFalse(game.isOver());
    }

    @Test
    void activeAndCheckPositionsAreNotTerminal() {
        Game active = new Game();
        Game checked = new Game(GameTestPositions.checkedPosition());

        assertEquals(GameStatus.ACTIVE, active.getState().getStatus());
        assertEquals(GameStatus.CHECK, checked.getState().getStatus());
        assertFalse(active.isOver());
        assertFalse(checked.isOver());
    }

    private void play(Game game, String... moves) {
        for (String notation : moves) {
            MoveResult result = game.playMove(Move.parse(notation));
            assertTrue(result.accepted(), result.message());
        }
    }
}
