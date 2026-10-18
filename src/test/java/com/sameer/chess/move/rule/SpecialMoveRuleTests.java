package com.sameer.chess.move.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.sameer.chess.TestBoards.place;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sameer.chess.board.Board;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;
import com.sameer.chess.move.MoveKind;

class SpecialMoveRuleTests {

    @Test
    void promotionRuleExpandsFinalRankPawnMoves() {
        Piece pawn = new Piece(Color.WHITE, Type.PAWN);
        Position from = new Position(1, 0);
        List<Move> candidates = List.of(new Move(from, new Position(0, 0)));

        List<Move> result = new PromotionRule().apply(
                pawn, from, candidates, new Board(), new GameState(Color.WHITE));

        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(move -> move.kind() == MoveKind.PROMOTION));
    }

    @Test
    void enPassantRuleAddsMoveOnlyForTheCurrentReplyOpportunity() {
        Board board = new Board();
        Piece whitePawn = place(board, Color.WHITE, Type.PAWN, 3, 4);
        Piece blackPawn = place(board, Color.BLACK, Type.PAWN, 1, 3);
        board.movePiece(new Position(1, 3), new Position(3, 3));
        GameState state = new GameState(Color.BLACK);
        state.recordMove(
                new Move(new Position(1, 3), new Position(3, 3)),
                blackPawn,
                null);

        List<Move> result = new EnPassantRule().apply(
                whitePawn, new Position(3, 4), List.of(), board, state);

        assertEquals(1, result.size());
        assertEquals(MoveKind.EN_PASSANT, result.getFirst().kind());
        assertEquals(new Position(2, 3), result.getFirst().to());
    }

    @Test
    void enPassantRuleRejectsStaleTargetsAndPassesThroughForNonPawns() {
        Board board = new Board();
        Piece whitePawn = place(board, Color.WHITE, Type.PAWN, 3, 4);
        Piece blackPawn = place(board, Color.BLACK, Type.PAWN, 1, 3);
        Piece whiteKnight = place(board, Color.WHITE, Type.KNIGHT, 7, 1);
        board.movePiece(new Position(1, 3), new Position(3, 3));
        GameState state = new GameState(Color.BLACK);
        state.recordMove(new Move(new Position(1, 3), new Position(3, 3)), blackPawn, null);
        state.recordMove(
                new Move(new Position(7, 1), new Position(5, 2)), whiteKnight, null);

        List<Move> staleResult = new EnPassantRule().apply(
                whitePawn, new Position(3, 4), List.of(), board, state);
        List<Move> candidates = List.of(new Move(new Position(7, 1), new Position(5, 2)));
        List<Move> nonPawnResult = new EnPassantRule().apply(
                whiteKnight, new Position(7, 1), candidates, board, state);

        assertTrue(staleResult.isEmpty());
        assertSame(candidates, nonPawnResult);
    }

    @Test
    void enPassantRuleSupportsBlackPawnCapturingWhiteDoubleStep() {
        Board board = new Board();
        Piece whitePawn = place(board, Color.WHITE, Type.PAWN, 6, 3);
        Piece blackPawn = place(board, Color.BLACK, Type.PAWN, 4, 4);
        board.movePiece(new Position(6, 3), new Position(4, 3));
        GameState state = new GameState(Color.WHITE);
        state.recordMove(new Move(new Position(6, 3), new Position(4, 3)), whitePawn, null);

        List<Move> result = new EnPassantRule().apply(
                blackPawn, new Position(4, 4), List.of(), board, state);

        assertEquals(List.of(new Move(
                new Position(4, 4), new Position(5, 3), null, MoveKind.EN_PASSANT)), result);
    }

    @Test
    void castlingRuleAddsCandidateWhenRightsAndPathAllowIt() {
        Board board = new Board();
        Piece king = place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.WHITE, Type.ROOK, 7, 7);
        GameState state = new GameState(Color.WHITE);
        state.setCastlingRights(Color.WHITE, true, false);

        List<Move> result = new CastlingRule().apply(
                king, new Position(7, 4), List.of(), board, state);

        assertEquals(1, result.size());
        assertEquals(MoveKind.CASTLING, result.getFirst().kind());
        assertEquals(new Position(7, 6), result.getFirst().to());
    }

    @Test
    void castlingRuleRejectsNoRightsAndBlockedPath() {
        Board board = new Board();
        Piece king = place(board, Color.WHITE, Type.KING, 7, 4);
        place(board, Color.WHITE, Type.ROOK, 7, 7);
        GameState noRights = new GameState(Color.WHITE);
        assertTrue(new CastlingRule().apply(
                king, new Position(7, 4), List.of(), board, noRights).isEmpty());

        GameState hasRights = new GameState(Color.WHITE);
        hasRights.setCastlingRights(Color.WHITE, true, false);
        place(board, Color.WHITE, Type.KNIGHT, 7, 5);
        assertTrue(new CastlingRule().apply(
                king, new Position(7, 4), List.of(), board, hasRights).isEmpty());
    }

    @Test
    void castlingRuleSupportsBlackSide() {
        Board board = new Board();
        Piece king = place(board, Color.BLACK, Type.KING, 0, 4);
        place(board, Color.BLACK, Type.ROOK, 0, 0);
        GameState state = new GameState(Color.BLACK);
        state.setCastlingRights(Color.BLACK, false, true);

        List<Move> result = new CastlingRule().apply(
                king, new Position(0, 4), List.of(), board, state);

        assertEquals(1, result.size());
        assertEquals(MoveKind.CASTLING, result.getFirst().kind());
        assertEquals(new Position(0, 2), result.getFirst().to());
    }

    @Test
    void promotionRulePassesThroughNonPawnCandidates() {
        Piece knight = new Piece(Color.WHITE, Type.KNIGHT);
        List<Move> candidates = List.of(new Move(new Position(1, 0), new Position(0, 0)));

        assertSame(candidates, new PromotionRule().apply(
                knight, new Position(1, 0), candidates, new Board(), new GameState(Color.WHITE)));
    }
}
