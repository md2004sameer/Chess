package com.sameer.chess.game;

import java.util.Locale;
import java.util.Scanner;
import com.sameer.chess.model.Move;
import com.sameer.chess.util.ConsoleRenderer;

/** Console entry point for the educational chess engine. */
public final class ChessEngine {
    private ChessEngine() {
    }

    public static void main(String[] args) {
        Game game = new Game();
        ConsoleRenderer renderer = new ConsoleRenderer();

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Chess Engine - enter moves such as e2e4 or e7e8q.");
            System.out.println("Commands: claim50, claim3fold, legal, quit");

            while (!game.isOver()) {
                renderer.render(game.getState());
                System.out.println("Status: " + game.getState().getStatus());
                System.out.print(game.getState().getActiveColor() + " to move > ");
                if (!scanner.hasNextLine()) break;

                String input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
                if (input.equals("quit")) break;
                if (input.isEmpty()) continue;
                if (input.equals("legal")) {
                    System.out.println(game.getLegalMoves());
                    continue;
                }
                if (input.equals("claim50")) {
                    System.out.println(game.claimFiftyMoveDraw()
                            ? "Draw claimed under the fifty-move rule."
                            : "A fifty-move draw cannot be claimed in this position.");
                    continue;
                }
                if (input.equals("claim3fold")) {
                    System.out.println(game.claimThreefoldRepetitionDraw()
                            ? "Draw claimed by threefold repetition."
                            : "A threefold-repetition draw cannot be claimed in this position.");
                    continue;
                }

                try {
                    Move move = Move.parse(input);
                    MoveResult result = game.playMove(move);
                    if (!result.accepted()) {
                        System.out.println("Move rejected: " + result.message());
                    } else {
                        System.out.println("Played " + move + ".");
                        if (result.status().isTerminal()) {
                            System.out.println("Game over: " + result.status());
                        }
                    }
                } catch (IllegalArgumentException exception) {
                    System.out.println("Invalid input: " + exception.getMessage());
                }
            }

            renderer.render(game.getState());
            System.out.println("Final status: " + game.getState().getStatus());
            if (!game.getHistory().isEmpty()) {
                System.out.println("Move history: " + game.getHistory());
            }
        }
    }
}
