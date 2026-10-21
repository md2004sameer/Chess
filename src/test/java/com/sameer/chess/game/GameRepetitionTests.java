package com.sameer.chess.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

class GameRepetitionTests {
    @Test
    void threefoldClaimIsUnavailableUntilTheThirdOccurrence() {
        Game game = new Game();
        shuffleKnights(game);
        assertFalse(game.canClaimThreefoldRepetitionDraw());

        shuffleKnights(game);

        assertTrue(game.canClaimThreefoldRepetitionDraw());
        assertTrue(game.claimThreefoldRepetitionDraw());
    }

    @Test
    void unusableEnPassantTargetDoesNotSplitEquivalentRepetitions() {
        Board board = repetitionBoard();
        GameTestPositions.put(board, "d5", Color.BLACK, PieceType.PAWN);
        Game game = new Game(GameTestPositions.state(board, Color.WHITE,
                GameTestPositions.noRights(), Position.parse("d6"), 0));

        shuffleKnights(game);
        shuffleKnights(game);

        assertTrue(game.canClaimThreefoldRepetitionDraw());
    }

    @Test
    void legalEnPassantAvailabilityDistinguishesThePositionKey() {
        Board board = repetitionBoard();
        GameTestPositions.put(board, "e5", Color.WHITE, PieceType.PAWN);
        GameTestPositions.put(board, "d5", Color.BLACK, PieceType.PAWN);
        Game game = new Game(GameTestPositions.state(board, Color.WHITE,
                GameTestPositions.noRights(), Position.parse("d6"), 0));

        shuffleKnights(game);
        shuffleKnights(game);
        assertFalse(game.canClaimThreefoldRepetitionDraw());

        shuffleKnights(game);

        assertTrue(game.canClaimThreefoldRepetitionDraw());
    }

    private Board repetitionBoard() {
        Board board = Board.empty();
        GameTestPositions.put(board, "a1", Color.WHITE, PieceType.KING);
        GameTestPositions.put(board, "h8", Color.BLACK, PieceType.KING);
        GameTestPositions.put(board, "b1", Color.WHITE, PieceType.KNIGHT);
        GameTestPositions.put(board, "b8", Color.BLACK, PieceType.KNIGHT);
        return board;
    }

    private void shuffleKnights(Game game) {
        for (String notation : new String[] {"b1c3", "b8c6", "c3b1", "c6b8"}) {
            MoveResult result = game.playMove(Move.parse(notation));
            assertTrue(result.accepted(), result.message());
        }
    }
}
