package com.sameer.chess.util;

import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;

/** Renders a game state as a text board for console play. */
public final class ConsoleRenderer {
    public void render(GameState state) {
        System.out.println();
        System.out.println("    a b c d e f g h");
        System.out.println("   -----------------");
        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " | ");
            for (int col = 0; col < 8; col++) {
                Piece piece = state.getBoard().getPiece(new Position(row, col));
                System.out.print((piece == null ? '.' : piece.displaySymbol()) + " ");
            }
            System.out.println("| " + (8 - row));
        }
        System.out.println("   -----------------");
        System.out.println("    a b c d e f g h");
    }
}
