package com.sameer.chess;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

/**
 * A single-file, console-playable chess engine example for Java 21.
 *
 * Compile: javac ChessEngine.java
 * Run:     java ChessEngine
 *
 * Coordinates use row 0 = rank 8 and row 7 = rank 1.
 * This is an educational engine focused on separation of responsibilities;
 * it does not implement a chess clock, undo/redo, PGN, or search/AI.
 */
public class ChessEngine {

    public static void main(String[] args) {
        Game game = new Game();
        ConsoleRenderer renderer = new ConsoleRenderer();

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Chess Engine - enter moves such as e2e4 or e7e8q.");
            System.out.println("Commands: claim50, claim3fold, legal, quit");

            while (!game.isOver()) {
                renderer.render(game.getState());
                System.out.println("Status: " + game.getState().getStatus());
                System.out.print(game.getState().getActiveColor() + " to move > ");

                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
                if (input.equals("quit")) {
                    break;
                }
                if (input.isEmpty()) {
                    continue;
                }
                if (input.equals("legal")) {
                    System.out.println(game.getLegalMoves());
                    continue;
                }
                if (input.equals("claim50")) {
                    System.out.println(game.claimFiftyMoveDraw()
                            ? "Draw claimed under the fifty-move rule."
                            : "A fifty-move draw cannot be claimed in this position.");
                    continue;
                }
                if (input.equals("claim3fold")) {
                    System.out.println(game.claimThreefoldRepetitionDraw()
                            ? "Draw claimed by threefold repetition."
                            : "A threefold-repetition draw cannot be claimed in this position.");
                    continue;
                }

                try {
                    Move move = Move.parse(input);
                    MoveResult result = game.playMove(move);
                    if (!result.accepted()) {
                        System.out.println("Move rejected: " + result.message());
                    } else {
                        System.out.println("Played " + move + ".");
                        if (result.status().isTerminal()) {
                            System.out.println("Game over: " + result.status());
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    System.out.println("Invalid input: " + ex.getMessage());
                }
            }

            renderer.render(game.getState());
            System.out.println("Final status: " + game.getState().getStatus());
            if (!game.getHistory().isEmpty()) {
                System.out.println("Move history: " + game.getHistory());
            }
        }
    }

    // ============================================================
    // Domain values
    // ============================================================

    public enum Color {
        WHITE, BLACK;

        public Color opposite() {
            return this == WHITE ? BLACK : WHITE;
        }

        int pawnDirection() {
            return this == WHITE ? -1 : 1;
        }

        int homeRow() {
            return this == WHITE ? 7 : 0;
        }

        int pawnStartRow() {
            return this == WHITE ? 6 : 1;
        }

        int promotionRow() {
            return this == WHITE ? 0 : 7;
        }
    }

    public enum PieceType {
        KING('K'), QUEEN('Q'), ROOK('R'), BISHOP('B'), KNIGHT('N'), PAWN('P');

        private final char symbol;

        PieceType(char symbol) {
            this.symbol = symbol;
        }

        public char symbol() {
            return symbol;
        }

        public static PieceType fromPromotionChar(char value) {
            return switch (Character.toLowerCase(value)) {
                case 'q' -> QUEEN;
                case 'r' -> ROOK;
                case 'b' -> BISHOP;
                case 'n' -> KNIGHT;
                default -> throw new IllegalArgumentException(
                        "Promotion must be q, r, b, or n.");
            };
        }
    }

    public enum GameStatus {
        ACTIVE,
        CHECK,
        CHECKMATE,
        STALEMATE,
        DRAW_FIFTY_MOVE,
        DRAW_THREEFOLD_REPETITION,
        DRAW_INSUFFICIENT_MATERIAL,
        DRAW_SEVENTY_FIVE_MOVE,
        DRAW_FIVEFOLD_REPETITION;

        public boolean isTerminal() {
            return this != ACTIVE && this != CHECK;
        }
    }

    public record Position(int row, int col) {
        public boolean isInsideBoard() {
            return row >= 0 && row < 8 && col >= 0 && col < 8;
        }

        public static Position parse(String square) {
            if (square == null || !square.matches("[a-h][1-8]")) {
                throw new IllegalArgumentException("Square must look like e2.");
            }
            int col = square.charAt(0) - 'a';
            int row = 8 - Character.digit(square.charAt(1), 10);
            return new Position(row, col);
        }

        @Override
        public String toString() {
            if (!isInsideBoard()) {
                return "(" + row + "," + col + ")";
            }
            return "" + (char) ('a' + col) + (char) ('8' - row);
        }
    }

    public record Move(Position from, Position to, PieceType promotionType) {
        public Move {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
        }

        public static Move parse(String input) {
            if (input == null || !input.matches("[a-h][1-8][a-h][1-8][qrbn]?")) {
                throw new IllegalArgumentException(
                        "Enter a move like e2e4 or a promotion like e7e8q.");
            }
            Position from = Position.parse(input.substring(0, 2));
            Position to = Position.parse(input.substring(2, 4));
            PieceType promotion = input.length() == 5
                    ? PieceType.fromPromotionChar(input.charAt(4))
                    : null;
            return new Move(from, to, promotion);
        }

        @Override
        public String toString() {
            return from + "" + to
                    + (promotionType == null ? "" : Character.toLowerCase(promotionType.symbol()));
        }
    }

    public record ValidationResult(boolean valid, String message) {
        public static ValidationResult ok() {
            return new ValidationResult(true, "OK");
        }

        public static ValidationResult reject(String message) {
            return new ValidationResult(false, message);
        }
    }

    public record MoveResult(boolean accepted, String message, GameStatus status) {
    }

    // ============================================================
    // Piece hierarchy: pieces hold identity; move strategies own movement logic.
    // ============================================================

    public abstract static class Piece {
        private final Color color;
        private final PieceType type;

        protected Piece(Color color, PieceType type) {
            this.color = Objects.requireNonNull(color);
            this.type = Objects.requireNonNull(type);
        }

        public Color getColor() {
            return color;
        }

        public PieceType getType() {
            return type;
        }

        public char displaySymbol() {
            char symbol = type.symbol();
            return color == Color.WHITE ? symbol : Character.toLowerCase(symbol);
        }

        @Override
        public String toString() {
            return String.valueOf(displaySymbol());
        }
    }

    public static final class King extends Piece {
        public King(Color color) { super(color, PieceType.KING); }
    }

    public static final class Queen extends Piece {
        public Queen(Color color) { super(color, PieceType.QUEEN); }
    }

    public static final class Rook extends Piece {
        public Rook(Color color) { super(color, PieceType.ROOK); }
    }

    public static final class Bishop extends Piece {
        public Bishop(Color color) { super(color, PieceType.BISHOP); }
    }

    public static final class Knight extends Piece {
        public Knight(Color color) { super(color, PieceType.KNIGHT); }
    }

    public static final class Pawn extends Piece {
        public Pawn(Color color) { super(color, PieceType.PAWN); }
    }

    private static Piece createPiece(PieceType type, Color color) {
        return switch (type) {
            case KING -> new King(color);
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            case PAWN -> new Pawn(color);
        };
    }

    // ============================================================
    // Board and game state
    // Board stores the position; it does not validate chess rules or control play.
    // ============================================================

    public static final class Board {
        private final Piece[][] squares;

        private Board() {
            this.squares = new Piece[8][8];
        }

        private Board(Piece[][] source) {
            this.squares = new Piece[8][8];
            for (int row = 0; row < 8; row++) {
                System.arraycopy(source[row], 0, this.squares[row], 0, 8);
            }
        }

        public static Board initialPosition() {
            Board board = new Board();
            PieceType[] backRank = {
                    PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                    PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
            };

            for (int col = 0; col < 8; col++) {
                board.setPiece(new Position(0, col), createPiece(backRank[col], Color.BLACK));
                board.setPiece(new Position(1, col), new Pawn(Color.BLACK));
                board.setPiece(new Position(6, col), new Pawn(Color.WHITE));
                board.setPiece(new Position(7, col), createPiece(backRank[col], Color.WHITE));
            }
            return board;
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

        private void setPiece(Position position, Piece piece) {
            if (!isInside(position)) {
                throw new IllegalArgumentException("Position is outside the board: " + position);
            }
            squares[position.row()][position.col()] = piece;
        }

        private Board copy() {
            // Piece objects are immutable, so a shallow copy of square references is safe.
            return new Board(squares);
        }
    }

    public static final class CastlingRights {
        private boolean whiteKingSide;
        private boolean whiteQueenSide;
        private boolean blackKingSide;
        private boolean blackQueenSide;

        public CastlingRights(boolean whiteKingSide, boolean whiteQueenSide,
                              boolean blackKingSide, boolean blackQueenSide) {
            this.whiteKingSide = whiteKingSide;
            this.whiteQueenSide = whiteQueenSide;
            this.blackKingSide = blackKingSide;
            this.blackQueenSide = blackQueenSide;
        }

        public boolean canCastle(Color color, boolean kingSide) {
            if (color == Color.WHITE) {
                return kingSide ? whiteKingSide : whiteQueenSide;
            }
            return kingSide ? blackKingSide : blackQueenSide;
        }

        private void disableKingSide(Color color) {
            if (color == Color.WHITE) whiteKingSide = false;
            else blackKingSide = false;
        }

        private void disableQueenSide(Color color) {
            if (color == Color.WHITE) whiteQueenSide = false;
            else blackQueenSide = false;
        }

        private void disableBoth(Color color) {
            disableKingSide(color);
            disableQueenSide(color);
        }

        private CastlingRights copy() {
            return new CastlingRights(
                    whiteKingSide, whiteQueenSide, blackKingSide, blackQueenSide);
        }

        private String key() {
            return "" + (whiteKingSide ? 'K' : '-') + (whiteQueenSide ? 'Q' : '-')
                    + (blackKingSide ? 'k' : '-') + (blackQueenSide ? 'q' : '-');
        }
    }

    public static final class GameState {
        private final Board board;
        private Color activeColor;
        private CastlingRights castlingRights;
        private Position enPassantTarget;
        private int halfmoveClock;
        private int fullmoveNumber;
        private GameStatus status;

        private GameState(Board board, Color activeColor, CastlingRights castlingRights,
                          Position enPassantTarget, int halfmoveClock, int fullmoveNumber,
                          GameStatus status) {
            this.board = board;
            this.activeColor = activeColor;
            this.castlingRights = castlingRights;
            this.enPassantTarget = enPassantTarget;
            this.halfmoveClock = halfmoveClock;
            this.fullmoveNumber = fullmoveNumber;
            this.status = status;
        }

        public static GameState initial() {
            return new GameState(
                    Board.initialPosition(), Color.WHITE,
                    new CastlingRights(true, true, true, true),
                    null, 0, 1, GameStatus.ACTIVE);
        }

        public Board getBoard() { return board; }
        public Color getActiveColor() { return activeColor; }
        public CastlingRights getCastlingRights() { return castlingRights; }
        public Position getEnPassantTarget() { return enPassantTarget; }
        public int getHalfmoveClock() { return halfmoveClock; }
        public int getFullmoveNumber() { return fullmoveNumber; }
        public GameStatus getStatus() { return status; }

        private GameState copy() {
            return new GameState(board.copy(), activeColor, castlingRights.copy(),
                    enPassantTarget, halfmoveClock, fullmoveNumber, status);
        }

        private String positionKey() {
            StringBuilder key = new StringBuilder(80);
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board.getPiece(new Position(row, col));
                    key.append(piece == null ? '.' : piece.displaySymbol());
                }
            }
            key.append('|').append(activeColor)
                    .append('|').append(castlingRights.key())
                    .append('|').append(enPassantTarget == null ? "-" : enPassantTarget);
            return key.toString();
        }
    }

    // ============================================================
    // Move generation: pseudo-legal candidates only
    // ============================================================

    public interface PieceMoveGenerator {
        List<Move> generate(GameState state, Position from);
    }

    public static final class MoveGenerator {
        private final Map<PieceType, PieceMoveGenerator> generators =
                new EnumMap<>(PieceType.class);

        public MoveGenerator() {
            generators.put(PieceType.KING, new KingMoveGenerator());
            generators.put(PieceType.QUEEN, new QueenMoveGenerator());
            generators.put(PieceType.ROOK, new RookMoveGenerator());
            generators.put(PieceType.BISHOP, new BishopMoveGenerator());
            generators.put(PieceType.KNIGHT, new KnightMoveGenerator());
            generators.put(PieceType.PAWN, new PawnMoveGenerator());
        }

        public List<Move> generatePseudoLegalMoves(GameState state, Position from) {
            if (!state.board.isInside(from)) return List.of();
            Piece piece = state.board.getPiece(from);
            if (piece == null) return List.of();
            PieceMoveGenerator generator = generators.get(piece.getType());
            return generator.generate(state, from);
        }
    }

    public abstract static class SlidingMoveGenerator implements PieceMoveGenerator {
        private final int[][] directions;

        protected SlidingMoveGenerator(int[][] directions) {
            this.directions = directions;
        }

        @Override
        public List<Move> generate(GameState state, Position from) {
            List<Move> moves = new ArrayList<>();
            Piece movingPiece = state.board.getPiece(from);
            for (int[] direction : directions) {
                int row = from.row() + direction[0];
                int col = from.col() + direction[1];
                while (isInside(row, col)) {
                    Position to = new Position(row, col);
                    Piece target = state.board.getPiece(to);
                    if (target == null) {
                        moves.add(new Move(from, to, null));
                    } else {
                        if (target.getColor() != movingPiece.getColor()
                                && target.getType() != PieceType.KING) {
                            moves.add(new Move(from, to, null));
                        }
                        break;
                    }
                    row += direction[0];
                    col += direction[1];
                }
            }
            return moves;
        }
    }

    public static final class RookMoveGenerator extends SlidingMoveGenerator {
        public RookMoveGenerator() {
            super(new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}});
        }
    }

    public static final class BishopMoveGenerator extends SlidingMoveGenerator {
        public BishopMoveGenerator() {
            super(new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}});
        }
    }

    public static final class QueenMoveGenerator extends SlidingMoveGenerator {
        public QueenMoveGenerator() {
            super(new int[][]{
                    {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                    {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
            });
        }
    }

    public static final class KnightMoveGenerator implements PieceMoveGenerator {
        private static final int[][] OFFSETS = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        @Override
        public List<Move> generate(GameState state, Position from) {
            List<Move> moves = new ArrayList<>();
            Piece movingPiece = state.board.getPiece(from);
            for (int[] offset : OFFSETS) {
                int row = from.row() + offset[0];
                int col = from.col() + offset[1];
                if (!isInside(row, col)) continue;
                Position to = new Position(row, col);
                Piece target = state.board.getPiece(to);
                if (target == null || (target.getColor() != movingPiece.getColor()
                        && target.getType() != PieceType.KING)) {
                    moves.add(new Move(from, to, null));
                }
            }
            return moves;
        }
    }

    public static final class KingMoveGenerator implements PieceMoveGenerator {
        @Override
        public List<Move> generate(GameState state, Position from) {
            List<Move> moves = new ArrayList<>();
            Piece king = state.board.getPiece(from);
            for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                for (int colOffset = -1; colOffset <= 1; colOffset++) {
                    if (rowOffset == 0 && colOffset == 0) continue;
                    int row = from.row() + rowOffset;
                    int col = from.col() + colOffset;
                    if (!isInside(row, col)) continue;
                    Position to = new Position(row, col);
                    Piece target = state.board.getPiece(to);
                    if (target == null || (target.getColor() != king.getColor()
                            && target.getType() != PieceType.KING)) {
                        moves.add(new Move(from, to, null));
                    }
                }
            }

            // These are candidates only; CastlingRule verifies every castling condition.
            int homeRow = king.getColor().homeRow();
            if (from.row() == homeRow && from.col() == 4) {
                if (state.castlingRights.canCastle(king.getColor(), true)) {
                    moves.add(new Move(from, new Position(homeRow, 6), null));
                }
                if (state.castlingRights.canCastle(king.getColor(), false)) {
                    moves.add(new Move(from, new Position(homeRow, 2), null));
                }
            }
            return moves;
        }
    }

    public static final class PawnMoveGenerator implements PieceMoveGenerator {
        private static final PieceType[] PROMOTIONS = {
                PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT
        };

        @Override
        public List<Move> generate(GameState state, Position from) {
            List<Move> moves = new ArrayList<>();
            Piece pawn = state.board.getPiece(from);
            int direction = pawn.getColor().pawnDirection();
            int oneRow = from.row() + direction;

            if (isInside(oneRow, from.col())) {
                Position oneForward = new Position(oneRow, from.col());
                if (state.board.isEmpty(oneForward)) {
                    addPawnMove(moves, from, oneForward, pawn.getColor());
                    int twoRow = from.row() + 2 * direction;
                    if (from.row() == pawn.getColor().pawnStartRow()
                            && state.board.isInside(new Position(twoRow, from.col()))) {
                        Position twoForward = new Position(twoRow, from.col());
                        if (state.board.isEmpty(twoForward)) {
                            moves.add(new Move(from, twoForward, null));
                        }
                    }
                }
            }

            for (int colOffset : new int[]{-1, 1}) {
                int targetRow = from.row() + direction;
                int targetCol = from.col() + colOffset;
                if (!isInside(targetRow, targetCol)) continue;
                Position targetPosition = new Position(targetRow, targetCol);
                Piece target = state.board.getPiece(targetPosition);

                if (target != null && target.getColor() != pawn.getColor()
                        && target.getType() != PieceType.KING) {
                    addPawnMove(moves, from, targetPosition, pawn.getColor());
                } else if (target == null && targetPosition.equals(state.enPassantTarget)) {
                    Position capturedPawnPosition = new Position(from.row(), targetCol);
                    Piece captured = state.board.getPiece(capturedPawnPosition);
                    if (captured instanceof Pawn && captured.getColor() != pawn.getColor()) {
                        moves.add(new Move(from, targetPosition, null));
                    }
                }
            }
            return moves;
        }

        private void addPawnMove(List<Move> moves, Position from, Position to, Color color) {
            if (to.row() == color.promotionRow()) {
                for (PieceType promotion : PROMOTIONS) {
                    moves.add(new Move(from, to, promotion));
                }
            } else {
                moves.add(new Move(from, to, null));
            }
        }
    }

    // ============================================================
    // Attack detection is separate from move generation.
    // This matters because pawn attacks include empty diagonal squares.
    // ============================================================

    public static final class AttackDetector {
        public boolean isSquareAttacked(Board board, Position target, Color attacker) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Position from = new Position(row, col);
                    Piece piece = board.getPiece(from);
                    if (piece == null || piece.getColor() != attacker) continue;
                    if (attacksSquare(board, from, target, piece)) return true;
                }
            }
            return false;
        }

        private boolean attacksSquare(Board board, Position from, Position target, Piece piece) {
            int rowDelta = target.row() - from.row();
            int colDelta = target.col() - from.col();
            int absRow = Math.abs(rowDelta);
            int absCol = Math.abs(colDelta);

            return switch (piece.getType()) {
                case PAWN -> rowDelta == piece.getColor().pawnDirection()
                        && absCol == 1;
                case KNIGHT -> absRow * absCol == 2;
                case KING -> Math.max(absRow, absCol) == 1;
                case BISHOP -> absRow == absCol && absRow > 0
                        && clearRay(board, from, target);
                case ROOK -> ((rowDelta == 0) != (colDelta == 0))
                        && clearRay(board, from, target);
                case QUEEN -> ((absRow == absCol && absRow > 0)
                        || ((rowDelta == 0) != (colDelta == 0)))
                        && clearRay(board, from, target);
            };
        }

        private boolean clearRay(Board board, Position from, Position target) {
            int rowStep = Integer.compare(target.row(), from.row());
            int colStep = Integer.compare(target.col(), from.col());
            int row = from.row() + rowStep;
            int col = from.col() + colStep;
            while (row != target.row() || col != target.col()) {
                if (board.getPiece(new Position(row, col)) != null) return false;
                row += rowStep;
                col += colStep;
            }
            return true;
        }

        public Position findKing(Board board, Color color) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Position position = new Position(row, col);
                    Piece piece = board.getPiece(position);
                    if (piece != null && piece.getColor() == color
                            && piece.getType() == PieceType.KING) {
                        return position;
                    }
                }
            }
            return null;
        }

        public boolean isInCheck(Board board, Color color) {
            Position kingPosition = findKing(board, color);
            return kingPosition != null
                    && isSquareAttacked(board, kingPosition, color.opposite());
        }
    }

    // ============================================================
    // Rule interfaces and implementations
    // ============================================================

    public interface MoveRule {
        ValidationResult validate(GameState state, Move move);
    }

    public static final class TurnRule implements MoveRule {
        @Override
        public ValidationResult validate(GameState state, Move move) {
            if (!state.board.isInside(move.from()) || !state.board.isInside(move.to())) {
                return ValidationResult.reject("Source or destination is outside the board.");
            }
            if (move.from().equals(move.to())) {
                return ValidationResult.reject("Source and destination must differ.");
            }
            Piece piece = state.board.getPiece(move.from());
            if (piece == null) {
                return ValidationResult.reject("There is no piece on the source square.");
            }
            if (piece.getColor() != state.activeColor) {
                return ValidationResult.reject("It is not that piece's turn to move.");
            }
            return ValidationResult.ok();
        }
    }

    public static final class DestinationRule implements MoveRule {
        @Override
        public ValidationResult validate(GameState state, Move move) {
            if (!state.board.isInside(move.to())) {
                return ValidationResult.reject("Destination is outside the board.");
            }
            Piece moving = state.board.getPiece(move.from());
            Piece target = state.board.getPiece(move.to());
            if (target != null && target.getColor() == moving.getColor()) {
                return ValidationResult.reject("A piece cannot capture a friendly piece.");
            }
            if (target != null && target.getType() == PieceType.KING) {
                return ValidationResult.reject("A king is checked or checkmated; it is not captured.");
            }
            return ValidationResult.ok();
        }
    }

    public static final class PseudoLegalMoveRule implements MoveRule {
        private final MoveGenerator generator;

        public PseudoLegalMoveRule(MoveGenerator generator) {
            this.generator = generator;
        }

        @Override
        public ValidationResult validate(GameState state, Move move) {
            List<Move> candidates = generator.generatePseudoLegalMoves(state, move.from());
            return candidates.contains(move)
                    ? ValidationResult.ok()
                    : ValidationResult.reject("The piece cannot make that move.");
        }
    }

    public static final class PromotionRule implements MoveRule {
        @Override
        public ValidationResult validate(GameState state, Move move) {
            Piece piece = state.board.getPiece(move.from());
            boolean pawnReachesLastRank = piece instanceof Pawn
                    && move.to().row() == piece.getColor().promotionRow();

            if (pawnReachesLastRank) {
                PieceType promotion = move.promotionType();
                if (promotion != PieceType.QUEEN && promotion != PieceType.ROOK
                        && promotion != PieceType.BISHOP && promotion != PieceType.KNIGHT) {
                    return ValidationResult.reject(
                            "A pawn reaching the last rank must promote to q, r, b, or n.");
                }
            } else if (move.promotionType() != null) {
                return ValidationResult.reject("Only a pawn reaching the last rank can promote.");
            }
            return ValidationResult.ok();
        }
    }

    public static final class EnPassantRule implements MoveRule {
        @Override
        public ValidationResult validate(GameState state, Move move) {
            Piece piece = state.board.getPiece(move.from());
            if (!(piece instanceof Pawn)) return ValidationResult.ok();

            boolean diagonal = move.from().col() != move.to().col();
            boolean destinationEmpty = state.board.isEmpty(move.to());
            if (!diagonal || !destinationEmpty) return ValidationResult.ok();

            if (!move.to().equals(state.enPassantTarget)) {
                return ValidationResult.reject("An empty diagonal pawn move must be en passant.");
            }
            Position capturedPosition = new Position(move.from().row(), move.to().col());
            Piece captured = state.board.getPiece(capturedPosition);
            if (!(captured instanceof Pawn) || captured.getColor() == piece.getColor()) {
                return ValidationResult.reject("No eligible opposing pawn can be captured en passant.");
            }
            return ValidationResult.ok();
        }
    }

    public static final class CastlingRule implements MoveRule {
        private final AttackDetector attackDetector;

        public CastlingRule(AttackDetector attackDetector) {
            this.attackDetector = attackDetector;
        }

        @Override
        public ValidationResult validate(GameState state, Move move) {
            Piece moving = state.board.getPiece(move.from());
            if (!(moving instanceof King)
                    || move.from().row() != move.to().row()
                    || Math.abs(move.to().col() - move.from().col()) != 2) {
                return ValidationResult.ok();
            }

            Color color = moving.getColor();
            int row = color.homeRow();
            boolean kingSide = move.to().col() == 6;
            if (move.from().row() != row || move.from().col() != 4
                    || (move.to().col() != 6 && move.to().col() != 2)) {
                return ValidationResult.reject("The king's castling move is not from its home square.");
            }
            if (!state.castlingRights.canCastle(color, kingSide)) {
                return ValidationResult.reject("Castling rights are no longer available.");
            }

            int rookCol = kingSide ? 7 : 0;
            Piece rook = state.board.getPiece(new Position(row, rookCol));
            if (!(rook instanceof Rook) || rook.getColor() != color) {
                return ValidationResult.reject("Castling requires the original-side rook on its corner.");
            }

            int[] emptyColumns = kingSide ? new int[]{5, 6} : new int[]{1, 2, 3};
            for (int col : emptyColumns) {
                if (!state.board.isEmpty(new Position(row, col))) {
                    return ValidationResult.reject("Squares between the king and rook must be empty.");
                }
            }

            // Remove the king from e-file while checking transit/destination attacks;
            // otherwise it could incorrectly block an enemy sliding attack ray.
            Board pathBoard = state.board.copy();
            pathBoard.setPiece(move.from(), null);
            int[] kingPathColumns = kingSide ? new int[]{4, 5, 6} : new int[]{4, 3, 2};
            for (int col : kingPathColumns) {
                if (attackDetector.isSquareAttacked(
                        pathBoard, new Position(row, col), color.opposite())) {
                    return ValidationResult.reject(
                            "The king cannot castle out of, through, or into check.");
                }
            }
            return ValidationResult.ok();
        }
    }

    public static final class KingSafetyRule implements MoveRule {
        private final MoveExecutor executor;
        private final AttackDetector attackDetector;

        public KingSafetyRule(MoveExecutor executor, AttackDetector attackDetector) {
            this.executor = executor;
            this.attackDetector = attackDetector;
        }

        @Override
        public ValidationResult validate(GameState state, Move move) {
            Color movingColor = state.activeColor;
            GameState simulated = state.copy();
            executor.applyUnchecked(simulated, move);
            Position king = attackDetector.findKing(simulated.board, movingColor);
            if (king == null) {
                return ValidationResult.reject("The moving side's king is missing.");
            }
            if (attackDetector.isSquareAttacked(
                    simulated.board, king, movingColor.opposite())) {
                return ValidationResult.reject("The move leaves your king in check.");
            }
            return ValidationResult.ok();
        }
    }

    public static final class MoveValidator {
        private final MoveGenerator generator;
        private final List<MoveRule> rules;

        public MoveValidator(MoveGenerator generator, MoveExecutor executor,
                             AttackDetector attackDetector) {
            this.generator = generator;
            this.rules = List.of(
                    new TurnRule(),
                    new DestinationRule(),
                    new PseudoLegalMoveRule(generator),
                    new PromotionRule(),
                    new EnPassantRule(),
                    new CastlingRule(attackDetector),
                    new KingSafetyRule(executor, attackDetector)
            );
        }

        public ValidationResult validate(GameState state, Move move) {
            for (MoveRule rule : rules) {
                ValidationResult result = rule.validate(state, move);
                if (!result.valid()) return result;
            }
            return ValidationResult.ok();
        }

        public List<Move> generateLegalMoves(GameState state) {
            List<Move> legalMoves = new ArrayList<>();
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Position from = new Position(row, col);
                    Piece piece = state.board.getPiece(from);
                    if (piece == null || piece.getColor() != state.activeColor) continue;
                    for (Move candidate : generator.generatePseudoLegalMoves(state, from)) {
                        if (validate(state, candidate).valid()) legalMoves.add(candidate);
                    }
                }
            }
            return List.copyOf(legalMoves);
        }

        public boolean hasAnyLegalMove(GameState state) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Position from = new Position(row, col);
                    Piece piece = state.board.getPiece(from);
                    if (piece == null || piece.getColor() != state.activeColor) continue;
                    for (Move candidate : generator.generatePseudoLegalMoves(state, from)) {
                        if (validate(state, candidate).valid()) return true;
                    }
                }
            }
            return false;
        }
    }

    // ============================================================
    // Move execution: mutates state only after Game validated a move.
    // ============================================================

    public static final class MoveExecutor {
        public void execute(GameState state, Move move) {
            applyUnchecked(state, move);
        }

        private void applyUnchecked(GameState state, Move move) {
            Board board = state.board;
            Piece moving = board.getPiece(move.from());
            if (moving == null) {
                throw new IllegalStateException("Cannot execute a move from an empty square.");
            }

            Position capturePosition = move.to();
            if (moving instanceof Pawn
                    && move.from().col() != move.to().col()
                    && board.isEmpty(move.to())
                    && move.to().equals(state.enPassantTarget)) {
                capturePosition = new Position(move.from().row(), move.to().col());
            }
            Piece captured = board.getPiece(capturePosition);

            updateCastlingRights(state.castlingRights, moving, move.from(), captured, capturePosition);

            boolean castling = moving instanceof King
                    && move.from().row() == move.to().row()
                    && Math.abs(move.from().col() - move.to().col()) == 2;

            board.setPiece(move.from(), null);
            if (!capturePosition.equals(move.to())) {
                board.setPiece(capturePosition, null);
            }

            Piece placedPiece = moving;
            if (move.promotionType() != null) {
                placedPiece = createPiece(move.promotionType(), moving.getColor());
            }
            board.setPiece(move.to(), placedPiece);

            if (castling) {
                int row = move.from().row();
                boolean kingSide = move.to().col() == 6;
                Position rookFrom = new Position(row, kingSide ? 7 : 0);
                Position rookTo = new Position(row, kingSide ? 5 : 3);
                Piece rook = board.getPiece(rookFrom);
                board.setPiece(rookFrom, null);
                board.setPiece(rookTo, rook);
            }

            state.enPassantTarget = null;
            if (moving instanceof Pawn && Math.abs(move.to().row() - move.from().row()) == 2) {
                state.enPassantTarget = new Position(
                        (move.from().row() + move.to().row()) / 2, move.from().col());
            }

            if (moving instanceof Pawn || captured != null) state.halfmoveClock = 0;
            else state.halfmoveClock++;

            if (moving.getColor() == Color.BLACK) state.fullmoveNumber++;
            state.activeColor = state.activeColor.opposite();
            state.status = GameStatus.ACTIVE;
        }

        private void updateCastlingRights(CastlingRights rights, Piece moving,
                                          Position from, Piece captured, Position capturePosition) {
            if (moving.getType() == PieceType.KING) {
                rights.disableBoth(moving.getColor());
            }
            if (moving.getType() == PieceType.ROOK) {
                clearRookRight(rights, moving.getColor(), from);
            }
            if (captured != null && captured.getType() == PieceType.ROOK) {
                clearRookRight(rights, captured.getColor(), capturePosition);
            }
        }

        private void clearRookRight(CastlingRights rights, Color color, Position rookSquare) {
            int homeRow = color.homeRow();
            if (rookSquare.row() != homeRow) return;
            if (rookSquare.col() == 0) rights.disableQueenSide(color);
            if (rookSquare.col() == 7) rights.disableKingSide(color);
        }
    }

    // ============================================================
    // Game-end rules and claimable draw rules
    // ============================================================

    public interface GameEndRule {
        Optional<GameStatus> evaluate(GameState state, int currentPositionRepetitions,
                                      boolean hasLegalMove, boolean inCheck);
    }

    public static final class CheckmateRule implements GameEndRule {
        @Override
        public Optional<GameStatus> evaluate(GameState state, int repetitions,
                                             boolean hasLegalMove, boolean inCheck) {
            return !hasLegalMove && inCheck
                    ? Optional.of(GameStatus.CHECKMATE) : Optional.empty();
        }
    }

    public static final class StalemateRule implements GameEndRule {
        @Override
        public Optional<GameStatus> evaluate(GameState state, int repetitions,
                                             boolean hasLegalMove, boolean inCheck) {
            return !hasLegalMove && !inCheck
                    ? Optional.of(GameStatus.STALEMATE) : Optional.empty();
        }
    }

    public static final class InsufficientMaterialRule implements GameEndRule {
        @Override
        public Optional<GameStatus> evaluate(GameState state, int repetitions,
                                             boolean hasLegalMove, boolean inCheck) {
            return hasInsufficientMaterial(state.board)
                    ? Optional.of(GameStatus.DRAW_INSUFFICIENT_MATERIAL) : Optional.empty();
        }

        private boolean hasInsufficientMaterial(Board board) {
            List<Position> bishops = new ArrayList<>();
            int knights = 0;

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board.getPiece(new Position(row, col));
                    if (piece == null || piece.getType() == PieceType.KING) continue;
                    switch (piece.getType()) {
                        case PAWN, ROOK, QUEEN -> { return false; }
                        case KNIGHT -> knights++;
                        case BISHOP -> bishops.add(new Position(row, col));
                        default -> { }
                    }
                }
            }

            int minors = knights + bishops.size();
            if (minors <= 1) return true;
            if (knights > 0) return false;

            // With bishops only, a same-colored-square bishop set cannot force mate.
            boolean firstSquareColor = (bishops.get(0).row() + bishops.get(0).col()) % 2 == 0;
            for (Position bishop : bishops) {
                boolean squareColor = (bishop.row() + bishop.col()) % 2 == 0;
                if (squareColor != firstSquareColor) return false;
            }
            return true;
        }
    }

    public static final class SeventyFiveMoveRule implements GameEndRule {
        @Override
        public Optional<GameStatus> evaluate(GameState state, int repetitions,
                                             boolean hasLegalMove, boolean inCheck) {
            return state.halfmoveClock >= 150
                    ? Optional.of(GameStatus.DRAW_SEVENTY_FIVE_MOVE) : Optional.empty();
        }
    }

    public static final class FivefoldRepetitionRule implements GameEndRule {
        @Override
        public Optional<GameStatus> evaluate(GameState state, int repetitions,
                                             boolean hasLegalMove, boolean inCheck) {
            return repetitions >= 5
                    ? Optional.of(GameStatus.DRAW_FIVEFOLD_REPETITION) : Optional.empty();
        }
    }

    public interface DrawClaimRule {
        boolean canClaim(GameState state, int currentPositionRepetitions);
    }

    public static final class FiftyMoveRule implements DrawClaimRule {
        @Override
        public boolean canClaim(GameState state, int repetitions) {
            return state.halfmoveClock >= 100;
        }
    }

    public static final class ThreefoldRepetitionRule implements DrawClaimRule {
        @Override
        public boolean canClaim(GameState state, int repetitions) {
            return repetitions >= 3;
        }
    }

    public static final class GameStatusEvaluator {
        private final MoveValidator validator;
        private final AttackDetector attackDetector;
        private final List<GameEndRule> automaticRules;

        public GameStatusEvaluator(MoveValidator validator, AttackDetector attackDetector) {
            this.validator = validator;
            this.attackDetector = attackDetector;
            this.automaticRules = List.of(
                    new CheckmateRule(),
                    new StalemateRule(),
                    new InsufficientMaterialRule(),
                    new SeventyFiveMoveRule(),
                    new FivefoldRepetitionRule()
            );
        }

        public GameStatus evaluate(GameState state, int repetitionCount) {
            boolean hasLegalMove = validator.hasAnyLegalMove(state);
            boolean inCheck = attackDetector.isInCheck(state.board, state.activeColor);
            for (GameEndRule rule : automaticRules) {
                Optional<GameStatus> result = rule.evaluate(
                        state, repetitionCount, hasLegalMove, inCheck);
                if (result.isPresent()) return result.get();
            }
            return inCheck ? GameStatus.CHECK : GameStatus.ACTIVE;
        }
    }

    // ============================================================
    // Game: application/use-case coordinator
    // ============================================================

    public static final class Game {
        private final GameState state;
        private final MoveGenerator moveGenerator;
        private final AttackDetector attackDetector;
        private final MoveExecutor moveExecutor;
        private final MoveValidator moveValidator;
        private final GameStatusEvaluator statusEvaluator;
        private final FiftyMoveRule fiftyMoveRule = new FiftyMoveRule();
        private final ThreefoldRepetitionRule threefoldRule = new ThreefoldRepetitionRule();
        private final List<Move> history = new ArrayList<>();
        private final Map<String, Integer> repetitionCounts = new HashMap<>();

        public Game() {
            this.state = GameState.initial();
            this.moveGenerator = new MoveGenerator();
            this.attackDetector = new AttackDetector();
            this.moveExecutor = new MoveExecutor();
            this.moveValidator = new MoveValidator(moveGenerator, moveExecutor, attackDetector);
            this.statusEvaluator = new GameStatusEvaluator(moveValidator, attackDetector);

            repetitionCounts.put(state.positionKey(), 1);
            state.status = statusEvaluator.evaluate(state, 1);
        }

        public GameState getState() {
            return state;
        }

        public List<Move> getHistory() {
            return List.copyOf(history);
        }

        public List<Move> getLegalMoves() {
            return moveValidator.generateLegalMoves(state);
        }

        public boolean isOver() {
            return state.status.isTerminal();
        }

        public MoveResult playMove(Move move) {
            if (isOver()) {
                return new MoveResult(false, "The game has already ended.", state.status);
            }
            if (move == null) {
                return new MoveResult(false, "Move cannot be null.", state.status);
            }

            ValidationResult validation = moveValidator.validate(state, move);
            if (!validation.valid()) {
                return new MoveResult(false, validation.message(), state.status);
            }

            moveExecutor.execute(state, move);
            history.add(move);
            int repetitions = repetitionCounts.merge(state.positionKey(), 1, Integer::sum);
            state.status = statusEvaluator.evaluate(state, repetitions);
            return new MoveResult(true, "Move accepted.", state.status);
        }

        public boolean canClaimFiftyMoveDraw() {
            return !isOver() && fiftyMoveRule.canClaim(state, currentRepetitionCount());
        }

        public boolean canClaimThreefoldRepetitionDraw() {
            return !isOver() && threefoldRule.canClaim(state, currentRepetitionCount());
        }

        public boolean claimFiftyMoveDraw() {
            if (!canClaimFiftyMoveDraw()) return false;
            state.status = GameStatus.DRAW_FIFTY_MOVE;
            return true;
        }

        public boolean claimThreefoldRepetitionDraw() {
            if (!canClaimThreefoldRepetitionDraw()) return false;
            state.status = GameStatus.DRAW_THREEFOLD_REPETITION;
            return true;
        }

        private int currentRepetitionCount() {
            return repetitionCounts.getOrDefault(state.positionKey(), 1);
        }
    }

    // ============================================================
    // Console rendering stays outside Board and GameState.
    // ============================================================

    public static final class ConsoleRenderer {
        public void render(GameState state) {
            System.out.println();
            System.out.println("    a b c d e f g h");
            System.out.println("   -----------------");
            for (int row = 0; row < 8; row++) {
                System.out.print((8 - row) + " | ");
                for (int col = 0; col < 8; col++) {
                    Piece piece = state.board.getPiece(new Position(row, col));
                    System.out.print((piece == null ? '.' : piece.displaySymbol()) + " ");
                }
                System.out.println("| " + (8 - row));
            }
            System.out.println("   -----------------");
            System.out.println("    a b c d e f g h");
            System.out.println("Full move: " + state.fullmoveNumber
                    + " | Halfmove clock: " + state.halfmoveClock
                    + " | En passant target: "
                    + (state.enPassantTarget == null ? "-" : state.enPassantTarget));
        }
    }

    private static boolean isInside(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
