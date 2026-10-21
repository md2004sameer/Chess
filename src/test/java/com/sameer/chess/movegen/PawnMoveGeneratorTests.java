package com.sameer.chess.movegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

class PawnMoveGeneratorTests {
    private final PawnMoveGenerator generator = new PawnMoveGenerator();

    @Test
    void generatesSingleAndDoublePushFromStartingRank() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "h8");
        GeneratorTestSupport.put(board, "e2", Color.WHITE, PieceType.PAWN);

        assertEquals(Set.of("e2e3", "e2e4"), GeneratorTestSupport.movesFrom(
                generator, GeneratorTestSupport.state(board), "e2"));
    }

    @Test
    void blockedPushPreventsSingleAndDoubleMoves() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "h8");
        GeneratorTestSupport.put(board, "e2", Color.WHITE, PieceType.PAWN);
        GeneratorTestSupport.put(board, "e3", Color.WHITE, PieceType.PAWN);

        assertTrue(GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board), "e2").isEmpty());
    }

    @Test
    void capturesEnemyDiagonallyButNeverCapturesKing() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "d5");
        GeneratorTestSupport.put(board, "e4", Color.WHITE, PieceType.PAWN);
        GeneratorTestSupport.put(board, "f5", Color.BLACK, PieceType.ROOK);

        Set<String> moves = GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board), "e4");

        assertTrue(moves.contains("e4f5"));
        assertFalse(moves.contains("e4d5"));
    }

    @Test
    void forwardAndCapturePromotionGenerateFourChoicesEach() {
        Board board = GeneratorTestSupport.boardWithKings("h1", "h8");
        GeneratorTestSupport.put(board, "a7", Color.WHITE, PieceType.PAWN);
        GeneratorTestSupport.put(board, "b8", Color.BLACK, PieceType.ROOK);

        Set<String> moves = GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board), "a7");

        assertEquals(Set.of("a7a8q", "a7a8r", "a7a8b", "a7a8n",
                "a7b8q", "a7b8r", "a7b8b", "a7b8n"), moves);
    }

    @Test
    void generatesEnPassantCandidate() {
        Board board = GeneratorTestSupport.boardWithKings("a1", "h8");
        GeneratorTestSupport.put(board, "e5", Color.WHITE, PieceType.PAWN);
        GeneratorTestSupport.put(board, "d5", Color.BLACK, PieceType.PAWN);

        assertTrue(GeneratorTestSupport.movesFrom(generator,
                GeneratorTestSupport.state(board, GeneratorTestSupport.noCastling(),
                        Position.parse("d6")), "e5").contains("e5d6"));
    }
}
