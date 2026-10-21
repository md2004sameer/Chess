package com.sameer.chess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.engine.AttackDetector;
import com.sameer.chess.engine.GameStatusEvaluator;
import com.sameer.chess.engine.MoveExecutor;
import com.sameer.chess.engine.MoveValidator;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.movegen.MoveGenerator;

class GameStatusEvaluatorTests {
    private final GameStatusEvaluator evaluator = new GameStatusEvaluator(
            MoveValidator.standard(new MoveGenerator(), new MoveExecutor(), new AttackDetector()),
            new AttackDetector());

    @Test
    void classifiesTheSameColorBishopAndBareKingPositionsAsInsufficient() {
        assertStatus(GameStatus.DRAW_INSUFFICIENT_MATERIAL,
                new Game(GameTestPositions.state(GameTestPositions.emptyKings(),
                        Color.WHITE, GameTestPositions.noRights(), null, 0)));
        assertStatus(GameStatus.DRAW_INSUFFICIENT_MATERIAL, gameWith(Color.WHITE,
                new PiecePlacement("c1", Color.WHITE, PieceType.BISHOP)));
        assertStatus(GameStatus.DRAW_INSUFFICIENT_MATERIAL, gameWith(Color.WHITE,
                new PiecePlacement("b1", Color.WHITE, PieceType.KNIGHT)));
        assertStatus(GameStatus.DRAW_INSUFFICIENT_MATERIAL, gameWith(Color.WHITE,
                new PiecePlacement("c1", Color.WHITE, PieceType.BISHOP),
                new PiecePlacement("f8", Color.BLACK, PieceType.BISHOP)));
    }

    @Test
    void doesNotCallNonDeadMinorOrMajorMaterialInsufficient() {
        assertNotInsufficient(new PiecePlacement("b1", Color.WHITE, PieceType.KNIGHT),
                new PiecePlacement("c1", Color.WHITE, PieceType.KNIGHT));
        assertNotInsufficient(new PiecePlacement("c1", Color.WHITE, PieceType.BISHOP),
                new PiecePlacement("f7", Color.BLACK, PieceType.BISHOP));
        assertNotInsufficient(new PiecePlacement("b2", Color.WHITE, PieceType.PAWN));
        assertNotInsufficient(new PiecePlacement("a2", Color.WHITE, PieceType.ROOK));
        assertNotInsufficient(new PiecePlacement("a2", Color.WHITE, PieceType.QUEEN));
    }

    @Test
    void distinguishesCheckmateFromStalemateBeforeAutomaticDraws() {
        GameState stalemate = stalemateAtSeventyFiveMoveThreshold();

        assertEquals(GameStatus.STALEMATE, evaluator.evaluate(stalemate, 5));
        assertTrue(new Game(stalemate).isOver());
    }

    @Test
    void leavesCheckmateHigherPriorityThanSeventyFiveMoveDraw() {
        Board board = Board.empty();
        GameTestPositions.put(board, "h8", Color.BLACK, PieceType.KING);
        GameTestPositions.put(board, "f6", Color.WHITE, PieceType.KING);
        GameTestPositions.put(board, "g7", Color.WHITE, PieceType.QUEEN);
        GameState checkmate = GameTestPositions.state(board, Color.BLACK,
                GameTestPositions.noRights(), null, 150);

        assertEquals(GameStatus.CHECKMATE, evaluator.evaluate(checkmate, 5));
    }

    @Test
    void returnsCheckWhenTheCheckedKingHasALegalResponse() {
        Game game = new Game(GameTestPositions.checkedPosition());

        assertEquals(GameStatus.CHECK, game.getState().getStatus());
        assertFalse(game.isOver());
        assertTrue(game.playMove(Move.parse("a2e2")).accepted());
    }

    private Game gameWith(Color active, PiecePlacement... placements) {
        Board board = GameTestPositions.emptyKings();
        for (PiecePlacement placement : placements) {
            GameTestPositions.put(board, placement.square(), placement.color(), placement.type());
        }
        return new Game(GameTestPositions.state(board, active,
                GameTestPositions.noRights(), null, 0));
    }

    private void assertNotInsufficient(PiecePlacement... placements) {
        Game game = gameWith(Color.WHITE, placements);
        assertFalse(game.getState().getStatus() == GameStatus.DRAW_INSUFFICIENT_MATERIAL,
                () -> "Unexpected insufficient-material draw: " + game.getState().getStatus());
    }

    private void assertStatus(GameStatus expected, Game game) {
        assertEquals(expected, game.getState().getStatus());
    }

    private GameState stalemateAtSeventyFiveMoveThreshold() {
        Board board = Board.empty();
        GameTestPositions.put(board, "h8", Color.BLACK, PieceType.KING);
        GameTestPositions.put(board, "f7", Color.WHITE, PieceType.KING);
        GameTestPositions.put(board, "g6", Color.WHITE, PieceType.QUEEN);
        return GameTestPositions.state(board, Color.BLACK,
                GameTestPositions.noRights(), null, 150);
    }

    private record PiecePlacement(String square, Color color, PieceType type) { }
}
