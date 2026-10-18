package com.sameer.chess.move.generator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.sameer.chess.TestBoards.assertMoves;
import static com.sameer.chess.TestBoards.move;
import static com.sameer.chess.TestBoards.place;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sameer.chess.board.Board;
import com.sameer.chess.board.BoardReader;
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

        assertMoves(moves,
                move(4, 3, 3, 3), move(4, 3, 2, 3),
                move(4, 3, 5, 3), move(4, 3, 6, 3), move(4, 3, 7, 3),
                move(4, 3, 4, 2), move(4, 3, 4, 1), move(4, 3, 4, 0), move(4, 3, 4, 4));
    }

    @Test
    void bishopMovesDiagonallyAndStopsAtBlocker() {
        Board board = new Board();
        place(board, Color.WHITE, Type.BISHOP, 4, 3);
        place(board, Color.BLACK, Type.PAWN, 2, 1);

        List<Move> moves = generate(Type.BISHOP, new Position(4, 3), board);

        assertMoves(moves,
                move(4, 3, 3, 2), move(4, 3, 2, 1),
                move(4, 3, 3, 4), move(4, 3, 2, 5), move(4, 3, 1, 6), move(4, 3, 0, 7),
                move(4, 3, 5, 2), move(4, 3, 6, 1), move(4, 3, 7, 0),
                move(4, 3, 5, 4), move(4, 3, 6, 5), move(4, 3, 7, 6));
    }

    @Test
    void queenCombinesRookAndBishopDirections() {
        Board board = new Board();
        place(board, Color.WHITE, Type.QUEEN, 3, 3);

        List<Move> moves = generate(Type.QUEEN, new Position(3, 3), board);

        assertMoves(moves,
                move(3, 3, 3, 0), move(3, 3, 3, 1), move(3, 3, 3, 2),
                move(3, 3, 3, 4), move(3, 3, 3, 5), move(3, 3, 3, 6), move(3, 3, 3, 7),
                move(3, 3, 0, 3), move(3, 3, 1, 3), move(3, 3, 2, 3),
                move(3, 3, 4, 3), move(3, 3, 5, 3), move(3, 3, 6, 3), move(3, 3, 7, 3),
                move(3, 3, 2, 2), move(3, 3, 1, 1), move(3, 3, 0, 0),
                move(3, 3, 2, 4), move(3, 3, 1, 5), move(3, 3, 0, 6),
                move(3, 3, 4, 2), move(3, 3, 5, 1), move(3, 3, 6, 0),
                move(3, 3, 4, 4), move(3, 3, 5, 5), move(3, 3, 6, 6), move(3, 3, 7, 7));
    }

    @Test
    void queenCanCaptureEnemyAndStopsBeforeFriendlyBlocker() {
        Board board = new Board();
        place(board, Color.WHITE, Type.QUEEN, 4, 4);
        place(board, Color.WHITE, Type.PAWN, 4, 6);
        place(board, Color.BLACK, Type.PAWN, 4, 2);

        List<Move> moves = generate(Type.QUEEN, new Position(4, 4), board);

        assertTrue(moves.contains(move(4, 4, 4, 2)));
        assertFalse(moves.contains(move(4, 4, 4, 6)));
        assertFalse(moves.contains(move(4, 4, 4, 7)));
    }

    @Test
    void sharedDirectionTablesAreImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> Directions.ALL.add(new Direction(2, 2)));
    }

    @Test
    void blackRookCanCaptureEnemyAndStopsAtFriendlyPiece() {
        Board board = new Board();
        place(board, Color.BLACK, Type.ROOK, 4, 4);
        place(board, Color.BLACK, Type.PAWN, 4, 5);
        place(board, Color.WHITE, Type.PAWN, 2, 4);

        List<Move> moves = generate(Type.ROOK, new Position(4, 4), board);

        assertTrue(moves.contains(move(4, 4, 2, 4)));
        assertFalse(moves.contains(move(4, 4, 4, 5)));
        assertFalse(moves.contains(move(4, 4, 4, 6)));
    }

    @Test
    void blackBishopCanCaptureEnemyAndStopsAtFriendlyPiece() {
        Board board = new Board();
        place(board, Color.BLACK, Type.BISHOP, 4, 4);
        place(board, Color.BLACK, Type.PAWN, 5, 5);
        place(board, Color.WHITE, Type.PAWN, 2, 2);

        List<Move> moves = generate(Type.BISHOP, new Position(4, 4), board);

        assertTrue(moves.contains(move(4, 4, 2, 2)));
        assertFalse(moves.contains(move(4, 4, 5, 5)));
        assertFalse(moves.contains(move(4, 4, 6, 6)));
    }

    @Test
    void blackKnightCanCaptureEnemyButNotLandOnFriendlyPiece() {
        Board board = new Board();
        place(board, Color.BLACK, Type.KNIGHT, 4, 4);
        place(board, Color.BLACK, Type.PAWN, 2, 5);
        place(board, Color.WHITE, Type.PAWN, 2, 3);

        List<Move> moves = generate(Type.KNIGHT, new Position(4, 4), board);

        assertTrue(moves.contains(move(4, 4, 2, 3)));
        assertFalse(moves.contains(move(4, 4, 2, 5)));
    }

    @Test
    void blackKingCanCaptureEnemyButNotLandOnFriendlyPiece() {
        Board board = new Board();
        place(board, Color.BLACK, Type.KING, 4, 4);
        place(board, Color.BLACK, Type.PAWN, 3, 4);
        place(board, Color.WHITE, Type.PAWN, 3, 3);

        List<Move> moves = generate(Type.KING, new Position(4, 4), board);

        assertTrue(moves.contains(move(4, 4, 3, 3)));
        assertFalse(moves.contains(move(4, 4, 3, 4)));
    }

    @Test
    void kingMovesOneSquareAndCannotLandOnFriendlyPiece() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 0, 0);
        place(board, Color.WHITE, Type.PAWN, 0, 1);

        List<Move> moves = generate(Type.KING, new Position(0, 0), board);

        assertMoves(moves, move(0, 0, 1, 0), move(0, 0, 1, 1));
    }

    @Test
    void whitePawnCanAdvanceOneOrTwoSquaresWhenClear() {
        Board board = new Board();
        place(board, Color.WHITE, Type.PAWN, 6, 3);

        List<Move> moves = generate(Type.PAWN, new Position(6, 3), board);

        assertMoves(moves, move(6, 3, 5, 3), move(6, 3, 4, 3));
    }

    @Test
    void whitePawnCannotDoubleStepWhenDestinationIsBlocked() {
        Board board = new Board();
        place(board, Color.WHITE, Type.PAWN, 6, 3);
        place(board, Color.BLACK, Type.KNIGHT, 4, 3);

        List<Move> moves = generate(Type.PAWN, new Position(6, 3), board);

        assertMoves(moves, move(6, 3, 5, 3));
    }

    @Test
    void blackPawnCanAdvanceTwoSquaresFromItsStartingRank() {
        Board board = new Board();
        place(board, Color.BLACK, Type.PAWN, 1, 3);
        BoardReader reader = board;

        List<Move> moves = registry.getGenerator(Type.PAWN)
                .generateMoves(new Position(1, 3), reader);

        assertMoves(moves, move(1, 3, 2, 3), move(1, 3, 3, 3));
    }

    @Test
    void pawnCannotDoubleStepWhenDestinationIsBlocked() {
        Board board = new Board();
        place(board, Color.BLACK, Type.PAWN, 1, 3);
        place(board, Color.WHITE, Type.KNIGHT, 3, 3);

        List<Move> moves = generate(Type.PAWN, new Position(1, 3), board);

        assertMoves(moves, move(1, 3, 2, 3));
    }

    @Test
    void pawnCannotAdvanceWhenItsFirstSquareIsBlocked() {
        Board board = new Board();
        place(board, Color.BLACK, Type.PAWN, 1, 3);
        place(board, Color.WHITE, Type.KNIGHT, 2, 3);

        List<Move> moves = generate(Type.PAWN, new Position(1, 3), board);

        assertMoves(moves);
    }

    @Test
    void pawnCapturesOnlyOpposingPiecesDiagonally() {
        Board board = new Board();
        place(board, Color.BLACK, Type.PAWN, 3, 3);
        place(board, Color.WHITE, Type.PAWN, 4, 2);
        place(board, Color.BLACK, Type.PAWN, 4, 4);
        place(board, Color.BLACK, Type.PAWN, 4, 3);

        List<Move> moves = generate(Type.PAWN, new Position(3, 3), board);

        assertMoves(moves, move(3, 3, 4, 2));
    }

    @Test
    void knightAtEdgeGeneratesOnlyOnBoardMoves() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KNIGHT, 0, 0);

        List<Move> moves = generate(Type.KNIGHT, new Position(0, 0), board);

        assertMoves(moves, move(0, 0, 1, 2), move(0, 0, 2, 1));
    }

    @Test
    void whitePawnCapturesOpposingPieceAndCannotDoubleStepOffStartingRow() {
        Board board = new Board();
        place(board, Color.WHITE, Type.PAWN, 4, 3);
        place(board, Color.BLACK, Type.KNIGHT, 3, 2);

        assertMoves(generate(Type.PAWN, new Position(4, 3), board),
                move(4, 3, 3, 3), move(4, 3, 3, 2));
    }

    @Test
    void pawnsOnLastRankAndOnEdgeFilesStayWithinBoard() {
        Board board = new Board();
        place(board, Color.WHITE, Type.PAWN, 0, 0);
        place(board, Color.BLACK, Type.PAWN, 7, 7);
        assertMoves(generate(Type.PAWN, new Position(0, 0), board));
        assertMoves(generate(Type.PAWN, new Position(7, 7), board));

        Board edgeBoard = new Board();
        place(edgeBoard, Color.WHITE, Type.PAWN, 6, 0);
        place(edgeBoard, Color.BLACK, Type.PAWN, 1, 7);
        assertMoves(generate(Type.PAWN, new Position(6, 0), edgeBoard),
                move(6, 0, 5, 0), move(6, 0, 4, 0));
        assertMoves(generate(Type.PAWN, new Position(1, 7), edgeBoard),
                move(1, 7, 2, 7), move(1, 7, 3, 7));
    }

    @Test
    void kingAndKnightGenerateExactCenterMovesIncludingCapture() {
        Board kingBoard = new Board();
        place(kingBoard, Color.WHITE, Type.KING, 4, 4);
        place(kingBoard, Color.BLACK, Type.PAWN, 3, 3);
        place(kingBoard, Color.WHITE, Type.PAWN, 3, 4);
        assertMoves(generate(Type.KING, new Position(4, 4), kingBoard),
                move(4, 4, 3, 3), move(4, 4, 3, 5), move(4, 4, 4, 3),
                move(4, 4, 4, 5), move(4, 4, 5, 3), move(4, 4, 5, 4),
                move(4, 4, 5, 5));

        Board knightBoard = new Board();
        place(knightBoard, Color.WHITE, Type.KNIGHT, 4, 4);
        place(knightBoard, Color.BLACK, Type.PAWN, 2, 3);
        place(knightBoard, Color.WHITE, Type.PAWN, 2, 5);
        assertMoves(generate(Type.KNIGHT, new Position(4, 4), knightBoard),
                move(4, 4, 2, 3), move(4, 4, 3, 2),
                move(4, 4, 3, 6), move(4, 4, 5, 2), move(4, 4, 5, 6),
                move(4, 4, 6, 3), move(4, 4, 6, 5));
    }

    @Test
    void rookAndBishopGenerateExactCornerMoves() {
        Board rookBoard = new Board();
        place(rookBoard, Color.WHITE, Type.ROOK, 0, 0);
        assertMoves(generate(Type.ROOK, new Position(0, 0), rookBoard),
                move(0, 0, 0, 1), move(0, 0, 0, 2), move(0, 0, 0, 3),
                move(0, 0, 0, 4), move(0, 0, 0, 5), move(0, 0, 0, 6), move(0, 0, 0, 7),
                move(0, 0, 1, 0), move(0, 0, 2, 0), move(0, 0, 3, 0),
                move(0, 0, 4, 0), move(0, 0, 5, 0), move(0, 0, 6, 0), move(0, 0, 7, 0));

        Board bishopBoard = new Board();
        place(bishopBoard, Color.WHITE, Type.BISHOP, 0, 0);
        assertMoves(generate(Type.BISHOP, new Position(0, 0), bishopBoard),
                move(0, 0, 1, 1), move(0, 0, 2, 2), move(0, 0, 3, 3),
                move(0, 0, 4, 4), move(0, 0, 5, 5), move(0, 0, 6, 6), move(0, 0, 7, 7));
    }

    @Test
    void emptyFromSquareGeneratesNoMoves() {
        assertMoves(generate(Type.KING, new Position(4, 4), new Board()));
    }

    @Test
    void generatorsReturnNoMovesWhenPieceTypeDoesNotMatch() {
        Position from = new Position(4, 4);
        for (Type actualType : Type.values()) {
            Board board = new Board();
            place(board, Color.WHITE, actualType, from.row(), from.col());

            for (Type generatorType : Type.values()) {
                if (generatorType != actualType) {
                    assertTrue(generate(generatorType, from, board).isEmpty(),
                            () -> generatorType + " generator accepted " + actualType);
                }
            }
        }
    }

    private List<Move> generate(Type type, Position from, Board board) {
        return registry.getGenerator(type).generateMoves(from, board);
    }

}
