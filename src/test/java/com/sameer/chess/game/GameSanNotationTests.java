package com.sameer.chess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Move;

class GameSanNotationTests {
    @Test
    void recordsStandardOpeningAndCastlingNotation() {
        Game game = new Game();
        play(game, "e2e4");
        play(game, "e7e5");
        play(game, "g1f3");
        play(game, "b8c6");
        play(game, "f1c4");
        play(game, "g8f6");
        play(game, "e1g1");

        assertEquals(
                java.util.List.of("e4", "e5", "Nf3", "Nc6", "Bc4", "Nf6", "O-O"),
                game.getSanHistory());
    }

    @Test
    void recordsCheckmateNotation() {
        Game game = new Game();
        play(game, "f2f3");
        play(game, "e7e5");
        play(game, "g2g4");
        play(game, "d8h4");

        assertEquals(java.util.List.of("f3", "e5", "g4", "Qh4#"), game.getSanHistory());
    }

    @Test
    void recordsOrdinaryAndEnPassantCaptures() {
        Game game = new Game();
        play(game, "e2e4");
        play(game, "d7d5");
        play(game, "e4d5");
        play(game, "a7a6");

        assertEquals(java.util.List.of("e4", "d5", "exd5", "a6"), game.getSanHistory());
    }

    @Test
    void recordsEnPassantCaptureNotation() {
        Game game = new Game();
        play(game, "e2e4");
        play(game, "a7a6");
        play(game, "e4e5");
        play(game, "d7d5");
        play(game, "e5d6");

        assertEquals("exd6", game.getSanHistory().getLast());
    }

    @Test
    void disambiguatesPiecesThatCanReachTheSameSquare() {
        Game gameFromBFile = gameWithTwoKnights();
        play(gameFromBFile, "b1d2");

        Game gameFromFFile = gameWithTwoKnights();
        play(gameFromFFile, "f3d2");

        assertEquals("Nbd2", gameFromBFile.getSanHistory().getFirst());
        assertEquals("Nfd2", gameFromFFile.getSanHistory().getFirst());
    }

    @Test
    void recordsPromotionChoice() {
        var board = GameTestPositions.emptyKings();
        GameTestPositions.put(board, "b7", Color.WHITE, PieceType.PAWN);
        Game game = new Game(GameTestPositions.state(
                board, Color.WHITE, GameTestPositions.noRights(), null, 0));

        play(game, "b7b8n");

        assertEquals("b8=N", game.getSanHistory().getFirst());
    }

    private Game gameWithTwoKnights() {
        var board = GameTestPositions.emptyKings();
        GameTestPositions.put(board, "b1", Color.WHITE, PieceType.KNIGHT);
        GameTestPositions.put(board, "f3", Color.WHITE, PieceType.KNIGHT);
        return new Game(GameTestPositions.state(
                board, Color.WHITE, GameTestPositions.noRights(), null, 0));
    }

    private void play(Game game, String move) {
        assertTrue(game.playMove(Move.parse(move)).accepted(), move);
    }
}
