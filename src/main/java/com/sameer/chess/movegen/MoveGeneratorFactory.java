package com.sameer.chess.movegen;

import com.sameer.chess.model.PieceType;

/** Factory method: callers ask for a generator by piece type and never construct concrete generators; add a piece rule by adding one class and one line here (Open/Closed). */
public final class MoveGeneratorFactory {
    private static final PieceMoveGenerator KING = new KingMoveGenerator();
    private static final PieceMoveGenerator QUEEN = new QueenMoveGenerator();
    private static final PieceMoveGenerator ROOK = new RookMoveGenerator();
    private static final PieceMoveGenerator BISHOP = new BishopMoveGenerator();
    private static final PieceMoveGenerator KNIGHT = new KnightMoveGenerator();
    private static final PieceMoveGenerator PAWN = new PawnMoveGenerator();

    private MoveGeneratorFactory() {
    }

    public static PieceMoveGenerator forPiece(PieceType type) {
        return switch (type) {
            case KING -> KING;
            case QUEEN -> QUEEN;
            case ROOK -> ROOK;
            case BISHOP -> BISHOP;
            case KNIGHT -> KNIGHT;
            case PAWN -> PAWN;
        };
    }
}
