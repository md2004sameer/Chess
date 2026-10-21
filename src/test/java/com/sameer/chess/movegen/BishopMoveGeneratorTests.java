package com.sameer.chess.movegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.PieceType;

class BishopMoveGeneratorTests {
    private final BishopMoveGenerator generator = new BishopMoveGenerator();

    @Test
    void generatesThirteenMovesFromAnOpenCenter() {
        Board board = GeneratorTestSupport.boardWithKings("a8", "h1");
        GeneratorTestSupport.put(board, "d4", Color.WHITE, PieceType.BISHOP);

        assertEquals(13, GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board), "d4").size());
    }

    @Test
    void ownPieceBlocksDiagonalBeyondIt() {
        Board board = GeneratorTestSupport.boardWithKings("a8", "h1");
        GeneratorTestSupport.put(board, "d4", Color.WHITE, PieceType.BISHOP);
        GeneratorTestSupport.put(board, "f6", Color.WHITE, PieceType.PAWN);

        Set<String> moves = GeneratorTestSupport.movesFrom(
                generator, GeneratorTestSupport.state(board), "d4");

        assertTrue(moves.contains("d4e5"));
        assertFalse(moves.contains("d4f6"));
        assertFalse(moves.contains("d4g7"));
    }

    @Test
    void capturesEnemyButNeverCapturesKing() {
        Board board = GeneratorTestSupport.boardWithKings("a8", "h1");
        GeneratorTestSupport.put(board, "d4", Color.WHITE, PieceType.BISHOP);
        GeneratorTestSupport.put(board, "f6", Color.BLACK, PieceType.ROOK);
        GeneratorTestSupport.put(board, "b6", Color.BLACK, PieceType.KING);

        Set<String> moves = GeneratorTestSupport.movesFrom(
                generator, GeneratorTestSupport.state(board), "d4");

        assertTrue(moves.contains("d4f6"));
        assertFalse(moves.contains("d4g7"));
        assertFalse(moves.contains("d4b6"));
        assertFalse(moves.contains("d4a7"));
    }
}
