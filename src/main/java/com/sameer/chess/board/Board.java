package com.sameer.chess.board;

import java.util.Objects;
import com.sameer.chess.model.Color;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

/** Stores pieces on the 8x8 board without applying game rules. */
public final class Board {
    private final Piece[][] squares;

    private Board() {
        squares = new Piece[8][8];
    }

    private Board(Piece[][] source) {
        squares = new Piece[8][8];
        for (int row = 0; row < 8; row++) {
            System.arraycopy(source[row], 0, squares[row], 0, 8);
        }
    }

    public static Board initialPosition() {
        Board board = new Board();
        PieceType[] backRank = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (int col = 0; col < 8; col++) {
            board.setPiece(new Position(0, col), new Piece(Color.BLACK, backRank[col]));
            board.setPiece(new Position(1, col), new Piece(Color.BLACK, PieceType.PAWN));
            board.setPiece(new Position(6, col), new Piece(Color.WHITE, PieceType.PAWN));
            board.setPiece(new Position(7, col), new Piece(Color.WHITE, backRank[col]));
        }
        return board;
    }

    public static Board empty() {
        return new Board();
    }

    public boolean isInside(Position position) {
        return position != null && position.isInsideBoard();
    }

    public Piece getPiece(Position position) {
        if (!isInside(position)) {
            throw new IllegalArgumentException("Position is outside the board: " + position);
        }
        return squares[position.row()][position.col()];
    }

    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }

    void placePiece(Position position, Piece piece) {
        setPiece(position, Objects.requireNonNull(piece, "piece"));
    }

    void removePiece(Position position) {
        setPiece(position, null);
    }

    /** Mutates one square for engine execution; engine-internal, do not call from outside the engine. */
    public void setPiece(Position position, Piece piece) {
        if (!isInside(position)) {
            throw new IllegalArgumentException("Position is outside the board: " + position);
        }
        squares[position.row()][position.col()] = piece;
    }

    /** Copies the board for safe simulations; engine-internal, do not call from outside the engine. */
    public Board copy() {
        return new Board(squares);
    }
}
