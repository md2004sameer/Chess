package com.sameer.chess.move.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sameer.chess.board.Board;
import com.sameer.chess.board.BoardReader;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;

class MoveGeneratorTests {

    private final MoveGeneratorRegistry registry =
            new MoveGeneratorConfiguration().moveGeneratorRegistry();

    @Test
    void rookStopsAtPiecesAndCanCaptureOpponent() {
        Board board = new Board();
        place(board, Color.WHITE, Type.ROOK, 4, 3);
        place(board, Color.WHITE, Type.PAWN, 4, 5);
        place(board, Color.BLACK, Type.PAWN, 2, 3);

        List<Move> moves = generate(Type.ROOK, new Position(4, 3), board);

        assertTrue(hasMove(moves, 4, 3, 2, 3));
        assertFalse(hasMove(moves, 4, 3, 4, 5));
        assertFalse(hasMove(moves, 4, 3, 4, 6));
    }

    @Test
    void bishopMovesDiagonallyAndStopsAtBlocker() {
        Board board = new Board();
        place(board, Color.WHITE, Type.BISHOP, 4, 3);
        place(board, Color.BLACK, Type.PAWN, 2, 1);

        List<Move> moves = generate(Type.BISHOP, new Position(4, 3), board);

        assertTrue(hasMove(moves, 4, 3, 2, 1));
        assertFalse(hasMove(moves, 4, 3, 1, 0));
        assertTrue(hasMove(moves, 4, 3, 3, 2));
        assertFalse(hasMove(moves, 4, 3, 3, 3));
    }

    @Test
    void queenCombinesRookAndBishopDirections() {
        Board board = new Board();
        place(board, Color.WHITE, Type.QUEEN, 3, 3);

        List<Move> moves = generate(Type.QUEEN, new Position(3, 3), board);

        assertTrue(hasMove(moves, 3, 3, 3, 7));
        assertTrue(hasMove(moves, 3, 3, 7, 7));
        assertEquals(27, moves.size());
    }

    @Test
    void kingMovesOneSquareAndCannotLandOnFriendlyPiece() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 0, 0);
        place(board, Color.WHITE, Type.PAWN, 0, 1);

        List<Move> moves = generate(Type.KING, new Position(0, 0), board);

        assertEquals(2, moves.size());
        assertTrue(hasMove(moves, 0, 0, 1, 0));
        assertTrue(hasMove(moves, 0, 0, 1, 1));
        assertFalse(hasMove(moves, 0, 0, 0, 1));
    }

    @Test
    void whitePawnCanAdvanceOneOrTwoSquaresWhenClear() {
        Board board = new Board();
        place(board, Color.WHITE, Type.PAWN, 6, 3);

        List<Move> moves = generate(Type.PAWN, new Position(6, 3), board);

        assertEquals(2, moves.size());
        assertTrue(hasMove(moves, 6, 3, 5, 3));
        assertTrue(hasMove(moves, 6, 3, 4, 3));
    }

    @Test
    void blackPawnCanAdvanceTwoSquaresFromItsStartingRank() {
        Board board = new Board();
        Piece pawn = place(board, Color.BLACK, Type.PAWN, 1, 3);
        BoardReader reader = board;

        List<Move> moves = registry.getGenerator(Type.PAWN)
                .generateMoves(new Position(1, 3), reader);

        assertEquals(2, moves.size());
        assertTrue(hasMove(moves, 1, 3, 2, 3));
        assertTrue(hasMove(moves, 1, 3, 3, 3));
        assertTrue(board.getPiece(new Position(1, 3)) == pawn);
    }

    @Test
    void pawnCannotDoubleStepWhenDestinationIsBlocked() {
        Board board = new Board();
        place(board, Color.BLACK, Type.PAWN, 1, 3);
        place(board, Color.WHITE, Type.KNIGHT, 3, 3);

        List<Move> moves = generate(Type.PAWN, new Position(1, 3), board);

        assertTrue(hasMove(moves, 1, 3, 2, 3));
        assertFalse(hasMove(moves, 1, 3, 3, 3));
    }

    @Test
    void pawnCannotAdvanceWhenItsFirstSquareIsBlocked() {
        Board board = new Board();
        place(board, Color.BLACK, Type.PAWN, 1, 3);
        place(board, Color.WHITE, Type.KNIGHT, 2, 3);

        List<Move> moves = generate(Type.PAWN, new Position(1, 3), board);

        assertTrue(moves.isEmpty());
    }

    @Test
    void pawnCapturesOnlyOpposingPiecesDiagonally() {
        Board board = new Board();
        place(board, Color.BLACK, Type.PAWN, 3, 3);
        place(board, Color.WHITE, Type.PAWN, 4, 2);
        place(board, Color.BLACK, Type.PAWN, 4, 4);
        place(board, Color.BLACK, Type.PAWN, 4, 3);

        List<Move> moves = generate(Type.PAWN, new Position(3, 3), board);

        assertTrue(hasMove(moves, 3, 3, 4, 2));
        assertFalse(hasMove(moves, 3, 3, 4, 4));
        assertFalse(hasMove(moves, 3, 3, 4, 3));
    }

    @Test
    void knightAtEdgeGeneratesOnlyOnBoardMoves() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KNIGHT, 0, 0);

        List<Move> moves = generate(Type.KNIGHT, new Position(0, 0), board);

        assertEquals(2, moves.size());
        assertTrue(hasMove(moves, 0, 0, 1, 2));
        assertTrue(hasMove(moves, 0, 0, 2, 1));
    }

    private List<Move> generate(Type type, Position from, Board board) {
        return registry.getGenerator(type).generateMoves(from, board);
    }

    private Piece place(Board board, Color color, Type type, int row, int col) {
        Piece piece = new Piece(color, type);
        board.placePiece(new Position(row, col), piece);
        return piece;
    }

    private boolean hasMove(List<Move> moves, int fromRow, int fromCol, int toRow, int toCol) {
        return moves.contains(new Move(
                new Position(fromRow, fromCol),
                new Position(toRow, toCol)));
    }
}
