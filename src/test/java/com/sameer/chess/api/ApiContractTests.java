package com.sameer.chess.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.sameer.chess.api.dto.GameDto;
import com.sameer.chess.api.dto.MoveRequest;
import com.sameer.chess.api.dto.PieceDto;
import com.sameer.chess.api.error.IllegalMoveException;

class ApiContractTests {
    @Test
    void moveRequestAllowsAnOmittedPromotion() {
        assertEquals(new MoveRequest("e2", "e4", null),
                new MoveRequest("e2", "e4", null));
    }

    @Test
    void gameSnapshotDefensivelyCopiesRowsAndAllowsEmptySquares() {
        List<PieceDto> row = new ArrayList<>();
        row.add(null);
        row.add(new PieceDto("WHITE", "KING", "K"));
        List<List<PieceDto>> board = new ArrayList<>();
        board.add(row);

        GameDto snapshot = new GameDto("id", "ACTIVE", "WHITE", board, 0);
        row.clear();
        board.clear();

        assertEquals(2, snapshot.board().getFirst().size());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.board().getFirst().add(null));
    }

    @Test
    void illegalMoveUsesAnExplicitUnprocessableStatusAndCode() {
        IllegalMoveException exception = new IllegalMoveException("Not legal.");

        assertEquals(422, exception.status().value());
        assertEquals("ILLEGAL_MOVE", exception.code());
    }
}
