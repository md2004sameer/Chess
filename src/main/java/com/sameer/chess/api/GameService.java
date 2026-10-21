package com.sameer.chess.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;
import com.sameer.chess.api.dto.GameDto;
import com.sameer.chess.api.dto.MoveDto;
import com.sameer.chess.api.dto.MoveRequest;
import com.sameer.chess.api.dto.PieceDto;
import com.sameer.chess.api.error.GameConflictException;
import com.sameer.chess.api.error.GameNotFoundException;
import com.sameer.chess.api.error.IllegalMoveException;
import com.sameer.chess.api.error.InvalidRequestException;
import com.sameer.chess.board.Board;
import com.sameer.chess.game.Game;
import com.sameer.chess.game.MoveResult;
import com.sameer.chess.model.Move;
import com.sameer.chess.model.Piece;
import com.sameer.chess.model.PieceType;
import com.sameer.chess.model.Position;

/** Owns in-memory games and translates engine values into stable API DTOs. */
@Service
public final class GameService {
    private final ConcurrentMap<UUID, GameSession> games = new ConcurrentHashMap<>();

    public GameDto createGame() {
        UUID gameId;
        GameSession session = new GameSession(Game.newGame());
        do {
            gameId = UUID.randomUUID();
        } while (games.putIfAbsent(gameId, session) != null);
        synchronized (session) {
            return toGameDto(gameId, session.game);
        }
    }

    public GameDto getGame(UUID gameId) {
        GameSession session = findSession(gameId);
        synchronized (session) {
            return toGameDto(gameId, session.game);
        }
    }

    public List<MoveDto> getLegalMoves(UUID gameId) {
        GameSession session = findSession(gameId);
        synchronized (session) {
            return session.game.legalMoves().stream().map(GameService::toMoveDto).toList();
        }
    }

    public GameDto playMove(UUID gameId, MoveRequest request) {
        Move move = toMove(request);
        GameSession session = findSession(gameId);
        synchronized (session) {
            Game game = session.game;
            if (game.isOver()) {
                throw new GameConflictException(
                        "The game has ended with status " + game.status() + ".");
            }
            MoveResult result = game.play(move);
            if (!result.accepted()) {
                throw new IllegalMoveException(result.message());
            }
            return toGameDto(gameId, game);
        }
    }

    public GameDto claimDraw(UUID gameId) {
        GameSession session = findSession(gameId);
        synchronized (session) {
            Game game = session.game;
            if (game.isOver()) {
                throw new GameConflictException(
                        "The game has ended with status " + game.status() + ".");
            }
            if (!game.claimDraw()) {
                throw new IllegalMoveException(
                        "A draw cannot be claimed under the fifty-move or threefold-repetition rules.");
            }
            return toGameDto(gameId, game);
        }
    }

    public GameDto resign(UUID gameId) {
        GameSession session = findSession(gameId);
        synchronized (session) {
            Game game = session.game;
            if (!game.resign()) {
                throw new GameConflictException(
                        "The game has ended with status " + game.status() + ".");
            }
            return toGameDto(gameId, game);
        }
    }

    public List<MoveDto> getMoveHistory(UUID gameId) {
        GameSession session = findSession(gameId);
        synchronized (session) {
            return session.game.getHistory().stream().map(GameService::toMoveDto).toList();
        }
    }

    private GameSession findSession(UUID gameId) {
        if (gameId == null) {
            throw new InvalidRequestException("A game UUID is required.");
        }
        GameSession session = games.get(gameId);
        if (session == null) {
            throw new GameNotFoundException(gameId);
        }
        return session;
    }

    private Move toMove(MoveRequest request) {
        if (request == null) {
            throw new InvalidRequestException("A move request body is required.");
        }
        Position from = parsePosition(request.from(), "from");
        Position to = parsePosition(request.to(), "to");
        PieceType promotion = parsePromotion(request.promotion());
        return new Move(from, to, promotion);
    }

    private Position parsePosition(String square, String field) {
        try {
            return Position.parse(square);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException(
                    "Field '" + field + "' must be an algebraic square from a1 through h8.");
        }
    }

    private PieceType parsePromotion(String promotion) {
        if (promotion == null) return null;
        if (promotion.length() != 1) {
            throw new InvalidRequestException("Promotion must be one of q, r, b, or n.");
        }
        try {
            return PieceType.fromPromotionChar(promotion.charAt(0));
        } catch (IllegalArgumentException exception) {
            throw new InvalidRequestException("Promotion must be one of q, r, b, or n.");
        }
    }

    private GameDto toGameDto(UUID gameId, Game game) {
        Board board = game.board();
        List<List<PieceDto>> rows = new ArrayList<>(8);
        for (int row = 0; row < 8; row++) {
            List<PieceDto> cells = new ArrayList<>(8);
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                cells.add(piece == null ? null : toPieceDto(piece));
            }
            rows.add(cells);
        }
        return new GameDto(
                gameId.toString(),
                game.status().name(),
                game.activeColor().name(),
                rows,
                game.getHistory().size(),
                game.canClaimDraw(),
                game.getSanHistory());
    }

    private PieceDto toPieceDto(Piece piece) {
        return new PieceDto(
                piece.color().name(),
                piece.type().name(),
                String.valueOf(piece.displaySymbol()));
    }

    private static MoveDto toMoveDto(Move move) {
        String promotion = move.promotionType() == null
                ? null
                : String.valueOf(Character.toLowerCase(move.promotionType().symbol()));
        return new MoveDto(move.from().toString(), move.to().toString(), promotion);
    }

    private static final class GameSession {
        private final Game game;

        private GameSession(Game game) {
            this.game = game;
        }
    }
}
