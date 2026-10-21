package com.sameer.chess.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Position;
import com.sameer.chess.movegen.MoveGenerator;

class LegalMovesApiTests {
    private final MoveValidator validator = MoveValidator.standard(
            new MoveGenerator(), new MoveExecutor(), new AttackDetector());

    @Test
    void legalMovesFromMatchesTheFullLegalMoveList() {
        GameState state = GameState.initial();
        Set<String> allMoves = moveStrings(validator.legalMoves(state));
        Set<String> fromE2 = moveStrings(validator.legalMovesFrom(state, Position.parse("e2")));

        assertEquals(Set.of("e2e3", "e2e4"), fromE2);
        assertTrue(allMoves.containsAll(fromE2));
    }

    private Set<String> moveStrings(Iterable<Move> moves) {
        Set<String> strings = new HashSet<>();
        for (Move move : moves) strings.add(move.toString());
        return strings;
    }
}
