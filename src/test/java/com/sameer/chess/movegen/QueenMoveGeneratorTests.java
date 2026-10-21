package com.sameer.chess.movegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.PieceType;

class QueenMoveGeneratorTests {
    private final QueenMoveGenerator generator = new QueenMoveGenerator();

    @Test
    void generatesTwentySevenMovesFromAnOpenCenter() {
        Board board = GeneratorTestSupport.boardWithKings("a8", "h1");
        GeneratorTestSupport.put(board, "d4", Color.WHITE, PieceType.QUEEN);

        assertEquals(27, GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board), "d4").size());
    }

    @Test
    void ownPieceStopsSlidingInItsDirection() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "h8");
        GeneratorTestSupport.put(board, "d4", Color.WHITE, PieceType.QUEEN);
        GeneratorTestSupport.put(board, "d6", Color.WHITE, PieceType.PAWN);

        Set<String> moves = GeneratorTestSupport.movesFrom(
                generator, GeneratorTestSupport.state(board), "d4");

        assertTrue(moves.contains("d4d5"));
        assertFalse(moves.contains("d4d6"));
        assertFalse(moves.contains("d4d7"));
    }

    @Test
    void capturesEnemyButNotKing() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "h8");
        GeneratorTestSupport.put(board, "d4", Color.WHITE, PieceType.QUEEN);
        GeneratorTestSupport.put(board, "d2", Color.BLACK, PieceType.ROOK);

        Set<String> moves = GeneratorTestSupport.movesFrom(
                generator, GeneratorTestSupport.state(board), "d4");

        assertTrue(moves.contains("d4d2"));
        assertFalse(moves.contains("d4d1"));
        assertFalse(moves.contains("d4h8"));
    }
}
