package com.sameer.chess.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.sameer.chess.model.Piece;
import com.sameer.chess.model.Position;
import com.sameer.chess.model.enums.Color;
import com.sameer.chess.model.enums.Type;

public class Board implements BoardReader, BoardWriter {

    private final Piece[][] squares = new Piece[8][8];

    @Override
    public Piece getPiece(Position position) {
        Objects.requireNonNull(position, "position must not be null");
        return squares[position.row()][position.col()];
    }

    @Override
    public boolean isOccupied(Position position) {
        return getPiece(position) != null;
    }

    @Override
    public boolean isInBounds(int row, int col) {
        return Position.isOnBoard(row, col);
    }

    @Override
    public void placePiece(Position position, Piece piece) {
        Objects.requireNonNull(position, "position must not be null");
        Objects.requireNonNull(piece, "piece must not be null");
        if (isOccupied(position)) {
            throw new IllegalArgumentException("Square is already occupied");
        }
        for (Position occupiedPosition : getOccupiedPositions()) {
            if (getPiece(occupiedPosition) == piece) {
                throw new IllegalArgumentException("Piece is already on the board");
            }
        }
        squares[position.row()][position.col()] = piece;
    }

    @Override
    public Piece removePiece(Position position) {
        Objects.requireNonNull(position, "position must not be null");
        Piece removedPiece = getPiece(position);
        squares[position.row()][position.col()] = null;
        return removedPiece;
    }

    @Override
    public void movePiece(Position from, Position to) {
        Objects.requireNonNull(from, "from position must not be null");
        Objects.requireNonNull(to, "to position must not be null");
        if (from.equals(to)) {
            throw new IllegalArgumentException("Source and destination must be different");
        }
        Piece piece = getPiece(from);
        if (piece == null) {
            throw new IllegalArgumentException("No piece at the source position");
        }
        if (isOccupied(to)) {
            throw new IllegalArgumentException("Destination position is occupied");
        }
        squares[from.row()][from.col()] = null;
        squares[to.row()][to.col()] = piece;
    }

    @Override
    public Position findKing(Color color) {
        Objects.requireNonNull(color, "color must not be null");
        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                Piece piece = squares[row][col];
                if (piece != null && piece.getColor() == color && piece.getType() == Type.KING) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }

    @Override
    public List<Position> getOccupiedPositions() {
        List<Position> positions = new ArrayList<>();
        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                if (squares[row][col] != null) {
                    positions.add(new Position(row, col));
                }
            }
        }
        return List.copyOf(positions);
    }

    public Board copy() {
        Board copy = new Board();
        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                Piece piece = squares[row][col];
                if (piece != null) {
                    copy.squares[row][col] = new Piece(piece.getColor(), piece.getType());
                }
            }
        }
        return copy;
    }
}
