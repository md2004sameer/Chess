package com.sameer.chess.movegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import com.sameer.chess.board.Board;
import com.sameer.chess.engine.AttackDetector;
import com.sameer.chess.engine.MoveExecutor;
import com.sameer.chess.engine.MoveValidator;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.CastlingRights;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.GameStatus;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

class PerftTests {
    private final MoveGenerator generator = new MoveGenerator();
    private final MoveExecutor executor = new MoveExecutor();
    private final MoveValidator validator = MoveValidator.standard(
            generator, executor, new AttackDetector());

    @Test
    void initialPositionHasExpectedLegalMoveCounts() {
        assertEquals(20, perft(GameState.initial(), 1, validator, executor));
        assertEquals(400, perft(GameState.initial(), 2, validator, executor));
        assertEquals(8_902, perft(GameState.initial(), 3, validator, executor));
    }

    @Test
    void kiwipeteHasExpectedLegalMoveCounts() {
        GameState state = fromFen(
                "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");

        assertEquals(48, perft(state, 1, validator, executor));
        assertEquals(2_039, perft(state, 2, validator, executor));
        assertEquals(97_862, perft(state, 3, validator, executor));
    }

    @Test
    void standardEndgamePositionHasExpectedLegalMoveCounts() {
        GameState state = fromFen(
                "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1");

        assertEquals(14, perft(state, 1, validator, executor));
        assertEquals(191, perft(state, 2, validator, executor));
        assertEquals(2_812, perft(state, 3, validator, executor));
        assertEquals(43_238, perft(state, 4, validator, executor));
    }

    @Test
    void standardPromotionAndCheckEvasionPositionHasExpectedCounts() {
        GameState state = fromFen(
                "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");

        assertEquals(6, perft(state, 1, validator, executor));
        assertEquals(264, perft(state, 2, validator, executor));
        assertEquals(9_467, perft(state, 3, validator, executor));
    }

    @Test
    void standardTacticalPositionHasExpectedLegalMoveCounts() {
        GameState state = fromFen(
                "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");

        assertEquals(44, perft(state, 1, validator, executor));
        assertEquals(1_486, perft(state, 2, validator, executor));
        assertEquals(62_379, perft(state, 3, validator, executor));
    }

    private GameState fromFen(String fen) {
        String[] fields = fen.split(" ");
        if (fields.length != 6) {
            throw new IllegalArgumentException("FEN must contain six fields.");
        }

        Board board = Board.empty();
        String[] ranks = fields[0].split("/");
        if (ranks.length != 8) {
            throw new IllegalArgumentException("FEN board must contain eight ranks.");
        }
        for (int row = 0; row < ranks.length; row++) {
            int col = 0;
            for (char symbol : ranks[row].toCharArray()) {
                if (Character.isDigit(symbol)) {
                    col += Character.digit(symbol, 10);
                } else {
                    Color color = Character.isUpperCase(symbol) ? Color.WHITE : Color.BLACK;
                    PieceType type = switch (Character.toLowerCase(symbol)) {
                        case 'k' -> PieceType.KING;
                        case 'q' -> PieceType.QUEEN;
                        case 'r' -> PieceType.ROOK;
                        case 'b' -> PieceType.BISHOP;
                        case 'n' -> PieceType.KNIGHT;
                        case 'p' -> PieceType.PAWN;
                        default -> throw new IllegalArgumentException("Invalid FEN piece.");
                    };
                    board.setPiece(new Position(row, col++), new Piece(color, type));
                }
            }
            if (col != 8) {
                throw new IllegalArgumentException("Each FEN rank must contain eight squares.");
            }
        }

        String castling = fields[2];
        CastlingRights rights = new CastlingRights(
                castling.contains("K"), castling.contains("Q"),
                castling.contains("k"), castling.contains("q"));
        Position enPassantTarget = fields[3].equals("-")
                ? null : Position.parse(fields[3]);
        return new GameState(
                board,
                fields[1].equals("w") ? Color.WHITE : Color.BLACK,
                rights,
                enPassantTarget,
                Integer.parseInt(fields[4]),
                Integer.parseInt(fields[5]),
                GameStatus.ACTIVE);
    }

    private long perft(GameState state, int depth, MoveValidator validator,
                       MoveExecutor executor) {
        if (depth == 0) return 1;
        long nodes = 0;
        for (Move move : validator.generateLegalMoves(state)) {
            GameState child = state.copy();
            executor.execute(child, move);
            nodes += perft(child, depth - 1, validator, executor);
        }
        return nodes;
    }
}
