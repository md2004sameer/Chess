package com.sameer.chess.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import com.sameer.chess.api.dto.GameDto;
import com.sameer.chess.api.dto.MoveDto;
import com.sameer.chess.api.dto.MoveRequest;
import com.sameer.chess.api.error.GameConflictException;
import com.sameer.chess.api.error.GameNotFoundException;
import com.sameer.chess.api.error.IllegalMoveException;
import com.sameer.chess.api.error.InvalidRequestException;

class GameServiceTests {
    private final GameService service = new GameService();

    @Test
    void createsUniqueGamesAndReturnsStableSnapshots() {
        GameDto first = service.createGame();
        GameDto second = service.createGame();

        assertNotEquals(first.gameId(), second.gameId());
        assertEquals("ACTIVE", first.status());
        assertEquals("WHITE", first.activeColor());
        assertEquals(8, first.board().size());
        assertEquals(8, first.board().getFirst().size());
        assertEquals(20, service.getLegalMoves(UUID.fromString(first.gameId())).size());
        assertEquals(0, first.moveCount());
    }

    @Test
    void successfulMovesUpdateTurnBoardAndMoveHistory() {
        UUID id = UUID.fromString(service.createGame().gameId());

        GameDto afterMove = service.playMove(id, new MoveRequest("e2", "e4", null));

        assertEquals("BLACK", afterMove.activeColor());
        assertEquals("P", afterMove.board().get(4).get(4).symbol());
        assertEquals(1, afterMove.moveCount());
        assertEquals(List.of("e4"), afterMove.sanHistory());
        assertEquals(List.of(new MoveDto("e2", "e4", null)), service.getMoveHistory(id));
    }

    @Test
    void rejectedAndWrongSideMovesLeaveTheSnapshotAndHistoryUnchanged() {
        UUID id = UUID.fromString(service.createGame().gameId());
        GameDto before = service.getGame(id);

        assertThrows(IllegalMoveException.class,
                () -> service.playMove(id, new MoveRequest("e7", "e5", null)));
        assertThrows(IllegalMoveException.class,
                () -> service.playMove(id, new MoveRequest("e2", "e5", null)));

        assertEquals(before, service.getGame(id));
        assertTrue(service.getMoveHistory(id).isEmpty());
    }

    @Test
    void promotionRequirementComesFromEngineAndPromotionIsRecorded() {
        UUID id = UUID.fromString(service.createGame().gameId());
        play(id, "a2", "a4");
        play(id, "h7", "h5");
        play(id, "a4", "a5");
        play(id, "h5", "h4");
        play(id, "a5", "a6");
        play(id, "h4", "h3");
        play(id, "a6", "b7");
        play(id, "b8", "c6");
        GameDto beforePromotion = service.getGame(id);

        IllegalMoveException missingPromotion = assertThrows(IllegalMoveException.class,
                () -> service.playMove(id, new MoveRequest("b7", "b8", null)));
        assertTrue(missingPromotion.getMessage().contains("must be promoted"));
        assertEquals(beforePromotion, service.getGame(id));

        GameDto promoted = service.playMove(id, new MoveRequest("b7", "b8", "q"));

        assertEquals("Q", promoted.board().getFirst().get(1).symbol());
        assertEquals(new MoveDto("b7", "b8", "q"), service.getMoveHistory(id).getLast());
    }

    @Test
    void malformedCoordinatesAndPromotionsAreBadRequests() {
        UUID id = UUID.fromString(service.createGame().gameId());

        assertThrows(InvalidRequestException.class,
                () -> service.playMove(id, new MoveRequest("e9", "e4", null)));
        assertThrows(InvalidRequestException.class,
                () -> service.playMove(id, new MoveRequest("e2", "e4", "king")));
    }

    @Test
    void finishedGamesRejectFurtherMovesAsConflicts() {
        UUID id = UUID.fromString(service.createGame().gameId());
        play(id, "f2", "f3");
        play(id, "e7", "e5");
        play(id, "g2", "g4");
        GameDto checkmate = service.playMove(id, new MoveRequest("d8", "h4", null));
        GameDto beforeRejectedMove = service.getGame(id);

        assertEquals("CHECKMATE", checkmate.status());
        assertThrows(GameConflictException.class,
                () -> service.playMove(id, new MoveRequest("a2", "a3", null)));
        assertEquals(beforeRejectedMove, service.getGame(id));
    }

    @Test
    void exposesAndClaimsDrawOnlyWhenThreefoldRepetitionIsEligible() {
        UUID id = UUID.fromString(service.createGame().gameId());

        assertFalse(service.getGame(id).canClaimDraw());
        assertThrows(IllegalMoveException.class, () -> service.claimDraw(id));

        for (int repetition = 0; repetition < 2; repetition++) {
            play(id, "b1", "c3");
            play(id, "b8", "c6");
            play(id, "c3", "b1");
            play(id, "c6", "b8");
        }

        assertTrue(service.getGame(id).canClaimDraw());
        GameDto drawn = service.claimDraw(id);
        assertEquals("DRAW_THREEFOLD_REPETITION", drawn.status());
        assertFalse(drawn.canClaimDraw());
        assertThrows(GameConflictException.class, () -> service.claimDraw(id));
    }

    @Test
    void resignationEndsGameAndRejectsLaterMoves() {
        UUID id = UUID.fromString(service.createGame().gameId());

        GameDto resigned = service.resign(id);

        assertEquals("RESIGNED", resigned.status());
        assertEquals("WHITE", resigned.activeColor());
        assertEquals(0, resigned.moveCount());
        assertThrows(GameConflictException.class,
                () -> service.playMove(id, new MoveRequest("e2", "e4", null)));
        assertThrows(GameConflictException.class, () -> service.resign(id));
    }

    @Test
    void unknownGamesAreNotFound() {
        assertThrows(GameNotFoundException.class, () -> service.getGame(UUID.randomUUID()));
    }

    @Test
    void concurrentAttemptsToPlayTheSameTurnSerializePerGame() throws Exception {
        UUID id = UUID.fromString(service.createGame().gameId());
        int attempts = 8;
        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        CountDownLatch ready = new CountDownLatch(attempts);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> results = new ArrayList<>(attempts);
            for (int i = 0; i < attempts; i++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        service.playMove(id, new MoveRequest("e2", "e4", null));
                        return true;
                    } catch (IllegalMoveException exception) {
                        return false;
                    }
                }));
            }
            ready.await();
            start.countDown();

            int successfulMoves = 0;
            for (Future<Boolean> result : results) {
                if (result.get()) successfulMoves++;
            }
            assertEquals(1, successfulMoves);
            assertEquals(1, service.getMoveHistory(id).size());
            assertEquals("BLACK", service.getGame(id).activeColor());
        } finally {
            executor.shutdownNow();
        }
    }

    private void play(UUID id, String from, String to) {
        service.playMove(id, new MoveRequest(from, to, null));
    }
}
