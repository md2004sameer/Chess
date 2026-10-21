package com.sameer.chess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

class GameApiTests {
    @Test
    void newGameFactoryExposesTheOriginalReadApi() {
        Game game = Game.newGame();

        assertEquals(Color.WHITE, game.activeColor());
        assertEquals(GameStatus.ACTIVE, game.status());
        assertEquals(20, game.legalMoves().size());
        assertEquals(game.getLegalMoves(), game.legalMoves());
    }

    @Test
    void playAliasUsesTheExistingValidationAndExecutionFlow() {
        Game game = Game.newGame();

        MoveResult result = game.play(Move.parse("e2e4"));

        assertTrue(result.accepted(), result.message());
        assertEquals(Color.BLACK, game.activeColor());
        assertEquals(GameStatus.ACTIVE, game.status());
        assertEquals(PieceType.PAWN, game.board().getPiece(Position.parse("e4")).type());
        assertEquals(1, game.getHistory().size());
    }

    @Test
    void playAliasPreservesRejectionBehavior() {
        Game game = Game.newGame();
        MoveResult result = game.play(Move.parse("e2e5"));

        assertFalse(result.accepted());
        assertEquals(Color.WHITE, game.activeColor());
        assertEquals(GameStatus.ACTIVE, game.status());
        assertTrue(game.getHistory().isEmpty());
        assertEquals(20, game.legalMoves().size());
    }
}
