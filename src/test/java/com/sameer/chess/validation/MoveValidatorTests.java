package com.sameer.chess.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.sameer.chess.TestBoards.move;
import static com.sameer.chess.TestBoards.place;
import static com.sameer.chess.TestBoards.validator;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sameer.chess.board.Board;
import com.sameer.chess.game.Game;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;
import com.sameer.chess.move.MoveExecutor;
import com.sameer.chess.move.MoveKind;

class MoveValidatorTests {

    private final MoveExecutor executor = new MoveExecutor();
    private final MoveValidator validator = validator();

    @Test
    void allowsCandidateMoveThatKeepsKingSafe() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.BLACK, Type.KING, 0, 4);
        place(board, Color.WHITE, Type.KNIGHT, 7, 1);

        assertTrue(validator.isLegalMove(board, move(7, 1, 5, 2), new GameState(Color.WHITE)));
    }

    @Test
    void rejectsMoveThatExposesKingToSlidingAttack() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.BLACK, Type.KING, 0, 0);
        place(board, Color.BLACK, Type.ROOK, 0, 4);
        place(board, Color.WHITE, Type.ROOK, 6, 4);

        assertFalse(validator.isLegalMove(board, move(6, 4, 6, 3), new GameState(Color.WHITE)));
    }

    @Test
    void legalMoveGenerationFiltersMovesThatExposeKing() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.BLACK, Type.KING, 0, 0);
        place(board, Color.BLACK, Type.ROOK, 0, 4);
        place(board, Color.WHITE, Type.ROOK, 6, 4);

        List<Move> legalMoves = validator.getLegalMoves(
                board, new Position(6, 4), new GameState(Color.WHITE));

        assertFalse(legalMoves.contains(move(6, 4, 6, 3)));
        assertTrue(legalMoves.contains(move(6, 4, 5, 4)));
    }

    @Test
    void legalKingMovesExcludeSquaresAttackedByOpponent() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 4, 4);
        place(board, Color.BLACK, Type.KING, 0, 0);
        place(board, Color.BLACK, Type.PAWN, 2, 3);

        List<Move> legalMoves = validator.getLegalMoves(
                board, new Position(4, 4), new GameState(Color.WHITE));

        assertFalse(legalMoves.contains(move(4, 4, 3, 4)));
        assertTrue(legalMoves.contains(move(4, 4, 4, 3)));
    }

    @Test
    void pawnPromotionOffersFourChoicesAndRequiresOneWhenValidated() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.BLACK, Type.KING, 0, 4);
        place(board, Color.WHITE, Type.PAWN, 1, 0);
        GameState state = new GameState(Color.WHITE);

        List<Move> legalMoves = validator.getLegalMoves(board, new Position(1, 0), state);

        assertTrue(legalMoves.stream().anyMatch(move -> move.promotionType() == Type.QUEEN));
        assertTrue(legalMoves.stream().anyMatch(move -> move.promotionType() == Type.ROOK));
        assertTrue(legalMoves.stream().anyMatch(move -> move.promotionType() == Type.BISHOP));
        assertTrue(legalMoves.stream().anyMatch(move -> move.promotionType() == Type.KNIGHT));
        assertFalse(validator.isLegalMove(board, move(1, 0, 0, 0), state));
        assertTrue(validator.isLegalMove(board,
                new Move(new Position(1, 0), new Position(0, 0), Type.KNIGHT, MoveKind.PROMOTION),
                state));
    }

    @Test
    void allowsEnPassantAfterImmediateOpposingDoublePawnMove() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.BLACK, Type.KING, 0, 4);
        place(board, Color.WHITE, Type.PAWN, 3, 4);
        place(board, Color.BLACK, Type.PAWN, 1, 3);
        Game game = new Game(board, new GameState(Color.BLACK), validator, executor);
        game.playMove(move(1, 3, 3, 3));

        List<Move> legalMoves = game.getLegalMoves(new Position(3, 4));

        assertTrue(legalMoves.stream().anyMatch(
                move -> move.isEnPassant() && move.to().equals(new Position(2, 3))));
    }

    @Test
    void castlingRequiresRightsAndUnattackedKingRoute() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.WHITE, Type.ROOK, 7, 7);
        place(board, Color.BLACK, Type.KING, 0, 0);
        GameState state = new GameState(Color.WHITE);
        state.setCastlingRights(Color.WHITE, true, false);

        List<Move> legalMoves = validator.getLegalMoves(board, new Position(7, 4), state);

        assertTrue(legalMoves.stream().anyMatch(
                move -> move.isCastling() && move.to().equals(new Position(7, 6))));

        place(board, Color.BLACK, Type.ROOK, 0, 5);
        legalMoves = validator.getLegalMoves(board, new Position(7, 4), state);
        assertFalse(legalMoves.stream().anyMatch(move -> move.isCastling()));
    }

    @Test
    void rejectsEnPassantThatExposesKingToSlidingAttack() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 3, 0);
        place(board, Color.BLACK, Type.KING, 0, 0);
        place(board, Color.BLACK, Type.ROOK, 3, 7);
        place(board, Color.WHITE, Type.PAWN, 3, 4);
        place(board, Color.BLACK, Type.PAWN, 1, 3);
        Game game = new Game(board, new GameState(Color.BLACK), validator, executor);
        game.playMove(move(1, 3, 3, 3));

        List<Move> legalMoves = game.getLegalMoves(new Position(3, 4));

        assertFalse(legalMoves.stream().anyMatch(move -> move.isEnPassant()));
    }

    @Test
    void rejectsKingMoveIntoPawnAttack() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 4, 4);
        place(board, Color.BLACK, Type.KING, 0, 0);
        place(board, Color.BLACK, Type.PAWN, 2, 3);

        assertFalse(validator.isLegalMove(board, move(4, 4, 3, 4), new GameState(Color.WHITE)));
    }

    @Test
    void rejectsMoveByOpponentOrNonCandidateMove() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.BLACK, Type.KING, 0, 4);
        place(board, Color.WHITE, Type.KNIGHT, 7, 1);

        assertFalse(validator.isLegalMove(board, move(7, 1, 6, 1), new GameState(Color.WHITE)));
        assertFalse(validator.isLegalMove(board, move(0, 4, 1, 4), new GameState(Color.WHITE)));
    }

    @Test
    void rejectsCapturingOpposingKing() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 0);
        place(board, Color.BLACK, Type.KING, 4, 7);
        place(board, Color.WHITE, Type.QUEEN, 4, 4);

        assertFalse(validator.isLegalMove(board, move(4, 4, 4, 7), new GameState(Color.WHITE)));
    }

    @Test
    void simulationDoesNotChangeOriginalBoardOrPiecePosition() {
        Board board = new Board();
        place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.BLACK, Type.KING, 0, 4);
        Piece knight = place(board, Color.WHITE, Type.KNIGHT, 7, 1);

        validator.isLegalMove(board, move(7, 1, 5, 2), new GameState(Color.WHITE));

        assertSame(knight, board.getPiece(new Position(7, 1)));
        assertNull(board.getPiece(new Position(5, 2)));
    }
}
