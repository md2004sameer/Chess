package com.sameer.chess.movegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;
import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.PieceType;

class KnightMoveGeneratorTests {
    private final KnightMoveGenerator generator = new KnightMoveGenerator();

    @Test
    void generatesEightMovesFromTheCenter() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "h8");
        GeneratorTestSupport.put(board, "d4", Color.WHITE, PieceType.KNIGHT);

        assertEquals(8, GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board), "d4").size());
    }

    @Test
    void generatesTwoMovesFromACorner() {
        Board board = GeneratorTestSupport.boardWithKings("h1", "h8");
        GeneratorTestSupport.put(board, "a1", Color.WHITE, PieceType.KNIGHT);

        assertEquals(2, GeneratorTestSupport.movesFrom(
                generator, GeneratorTestSupport.state(board), "a1").size());
    }

    @Test
    void capturesEnemyAndCannotJumpToFriendlyPiece() {
        Board board = GeneratorTestSupport.boardWithKings("h1", "h8");
        GeneratorTestSupport.put(board, "a1", Color.WHITE, PieceType.KNIGHT);
        GeneratorTestSupport.put(board, "b3", Color.WHITE, PieceType.PAWN);
        GeneratorTestSupport.put(board, "c2", Color.BLACK, PieceType.ROOK);

        Set<String> moves = GeneratorTestSupport.movesFrom(
                generator, GeneratorTestSupport.state(board), "a1");

        assertEquals(Set.of("a1c2"), moves);
    }

    @Test
    void cannotCaptureEnemyKing() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "c2");
        GeneratorTestSupport.put(board, "a1", Color.WHITE, PieceType.KNIGHT);

        assertFalse(GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board), "a1").contains("a1c2"));
    }
}
