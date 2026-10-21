package com.sameer.chess.util;

import java.util.Objects;
import com.sameer.chess.board.Board;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;

/** Prints a board position as text for tests and debugging. */
public final class BoardPrinter {
    private BoardPrinter() { }

    public static String print(Board board) {
        Objects.requireNonNull(board, "board");
        StringBuilder output = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            output.append(8 - row).append(' ');
            appendRow(board, row, output);
            output.append('\n');
        }
        return output.append("  a b c d e f g h").toString();
    }

    private static void appendRow(Board board, int row, StringBuilder output) {
        for (int col = 0; col < 8; col++) {
            if (col > 0) output.append(' ');
            Piece piece = board.getPiece(new Position(row, col));
            output.append(piece == null ? '.' : piece.displaySymbol());
        }
    }
}
