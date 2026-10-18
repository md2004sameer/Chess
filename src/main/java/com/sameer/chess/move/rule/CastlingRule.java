package com.sameer.chess.move.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sameer.chess.board.BoardReader;
import com.sameer.chess.game.GameState;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Type;
import com.sameer.chess.move.Castling;
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
        int homeRow = piece.getColor().homeRow();
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
        Castling castling = Castling.forSide(kingSide);
        Piece rook = board.getPiece(castling.rookStart(from));
        if (rook == null || rook.getType() != Type.ROOK || rook.getColor() != king.getColor()) {
            return;
        }
        if (!castling.hasClearPath(board, from)) {
            return;
        }
        moves.add(new Move(
                from,
                castling.kingDestination(from),
                null,
                MoveKind.CASTLING));
    }
}
