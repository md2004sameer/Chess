package com.sameer.chess.movegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.PieceType;

class RookMoveGeneratorTests {
    private final RookMoveGenerator generator = new RookMoveGenerator();

    @Test
    void generatesFourteenMovesFromAnOpenCenter() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "h8");
        GeneratorTestSupport.put(board, "d4", Color.WHITE, PieceType.ROOK);

        assertEquals(14, GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board), "d4").size());
    }

    @Test
    void ownPieceBlocksRookRay() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "h8");
        GeneratorTestSupport.put(board, "d4", Color.WHITE, PieceType.ROOK);
        GeneratorTestSupport.put(board, "d6", Color.WHITE, PieceType.BISHOP);

        Set<String> moves = GeneratorTestSupport.movesFrom(
                generator, GeneratorTestSupport.state(board), "d4");

        assertTrue(moves.contains("d4d5"));
        assertFalse(moves.contains("d4d6"));
        assertFalse(moves.contains("d4d7"));
    }

    @Test
    void capturesEnemyAndStopsBeforeEnemyKing() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "h8");
        GeneratorTestSupport.put(board, "d4", Color.WHITE, PieceType.ROOK);
        GeneratorTestSupport.put(board, "d2", Color.BLACK, PieceType.BISHOP);
        GeneratorTestSupport.put(board, "d6", Color.BLACK, PieceType.KING);

        Set<String> moves = GeneratorTestSupport.movesFrom(
                generator, GeneratorTestSupport.state(board), "d4");

        assertTrue(moves.contains("d4d2"));
        assertFalse(moves.contains("d4d1"));
        assertFalse(moves.contains("d4d6"));
        assertFalse(moves.contains("d4d7"));
    }
}
