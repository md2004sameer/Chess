package com.sameer.chess.movegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.CastlingRights;

class KingMoveGeneratorTests {
    private final KingMoveGenerator generator = new KingMoveGenerator();

    @Test
    void generatesEightMovesFromTheCenter() {
        Board board = GeneratorTestSupport.boardWithKings("d4", "h8");

        assertEquals(8, GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board), "d4").size());
    }

    @Test
    void generatesThreeMovesFromTheCornerAndCannotCaptureKing() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "b2");
        Set<String> moves = GeneratorTestSupport.movesFrom(
                generator, GeneratorTestSupport.state(board), "a1");

        assertEquals(2, moves.size());
        assertFalse(moves.contains("a1b2"));
    }

    @Test
    void castlingCandidatesRequireAvailableRights() {
        Board board = GeneratorTestSupport.boardWithKings("e1", "e8");
        CastlingRights rights = new CastlingRights(true, true, false, false);

        Set<String> moves = GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board, rights, null), "e1");

        assertTrue(moves.contains("e1g1"));
        assertTrue(moves.contains("e1c1"));
        assertFalse(GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board), "e1").contains("e1g1"));
    }
}
