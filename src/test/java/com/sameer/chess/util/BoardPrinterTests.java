package com.sameer.chess.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;

class BoardPrinterTests {
    @Test
    void printsInitialPositionWithRanksFilesAndPieceSymbols() {
        String expected = """
                8 r n b q k b n r
                7 p p p p p p p p
                6 . . . . . . . .
                5 . . . . . . . .
                4 . . . . . . . .
                3 . . . . . . . .
                2 P P P P P P P P
                1 R N B Q K B N R
                  a b c d e f g h""";

        assertEquals(expected, BoardPrinter.print(Board.initialPosition()));
    }

    @Test
    void rejectsANullBoard() {
        assertThrows(NullPointerException.class, () -> BoardPrinter.print(null));
    }
}
