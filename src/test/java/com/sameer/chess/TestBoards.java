package com.sameer.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import com.sameer.chess.board.Board;
import com.sameer.chess.game.Game;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;
import com.sameer.chess.move.MoveExecutor;
import com.sameer.chess.move.generator.MoveGeneratorConfiguration;
import com.sameer.chess.move.rule.CastlingRule;
import com.sameer.chess.move.rule.EnPassantRule;
import com.sameer.chess.move.rule.PromotionRule;
import com.sameer.chess.validation.AttackDetector;
import com.sameer.chess.validation.MoveValidator;

public final class TestBoards {

    private TestBoards() {
    }

    public static Piece place(Board board, Color color, Type type, int row, int col) {
        Piece piece = new Piece(color, type);
        board.placePiece(new Position(row, col), piece);
        return piece;
    }

    public static Piece place(Board board, Color color, Type type, Position position) {
        Piece piece = new Piece(color, type);
        board.placePiece(position, piece);
        return piece;
    }

    public static Move move(int fromRow, int fromCol, int toRow, int toCol) {
        return new Move(new Position(fromRow, fromCol), new Position(toRow, toCol));
    }

    public static void assertMoves(List<Move> actual, Move... expected) {
        assertEquals(expected.length, actual.size(), "Unexpected number of generated moves");
        assertEquals(Set.of(expected), Set.copyOf(actual));
    }

    public static MoveValidator validator() {
        return new MoveValidator(
                new MoveGeneratorConfiguration().moveGeneratorRegistry(),
                List.of(new CastlingRule(), new EnPassantRule(), new PromotionRule()),
                new AttackDetector(),
                new MoveExecutor());
    }

    public static Game game(Board board, GameState state) {
        return new Game(board, state, validator(), new MoveExecutor());
    }
}
