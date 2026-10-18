package com.sameer.chess.move.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Move;
import com.sameer.chess.move.MoveKind;

@Component
public class CastlingRule implements SpecialMoveRule {

    @Override
    public List<Move> apply(
            Piece piece,
            Position from,
            List<Move> candidates,
            BoardReader board,
            GameState gameState) {
        if (piece.getType() != Type.KING) {
            return candidates;
        }
        int homeRow = piece.getColor() == Color.WHITE ? 7 : 0;
        if (from.row() != homeRow || from.col() != 4) {
            return candidates;
        }
        List<Move> result = new ArrayList<>(candidates);
        addCastlingMove(result, from, piece, board, gameState, true);
        addCastlingMove(result, from, piece, board, gameState, false);
        return List.copyOf(result);
    }

    private void addCastlingMove(
            List<Move> moves,
            Position from,
            Piece king,
            BoardReader board,
            GameState gameState,
            boolean kingSide) {
        if (!gameState.canCastle(king.getColor(), kingSide)) {
            return;
        }
        int homeRow = from.row();
        int rookCol = kingSide ? 7 : 0;
        Piece rook = board.getPiece(new Position(homeRow, rookCol));
        if (rook == null || rook.getType() != Type.ROOK || rook.getColor() != king.getColor()) {
            return;
        }
        int step = kingSide ? 1 : -1;
        for (int col = from.col() + step; col != rookCol; col += step) {
            if (board.isOccupied(new Position(homeRow, col))) {
                return;
            }
        }
        moves.add(new Move(
                from,
                new Position(homeRow, kingSide ? 6 : 2),
                null,
                null,
                MoveKind.CASTLING));
    }
}
