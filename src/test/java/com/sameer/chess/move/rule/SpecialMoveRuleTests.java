package com.sameer.chess.move.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private Piece place(Board board, Color color, Type type, int row, int col) {
        Piece piece = new Piece(color, type);
        board.placePiece(new Position(row, col), piece);
        return piece;
    }
}
