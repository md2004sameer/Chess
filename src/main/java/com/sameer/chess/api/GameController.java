package com.sameer.chess.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sameer.chess.api.dto.GameDto;
import com.sameer.chess.api.dto.MoveDto;
import com.sameer.chess.api.dto.MoveRequest;

/** HTTP endpoints for creating and operating independent chess games. */
@RestController
@RequestMapping("/api/games")
public final class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameDto> createGame() {
        GameDto game = gameService.createGame();
        return ResponseEntity.created(URI.create("/api/games/" + game.gameId())).body(game);
    }

    @GetMapping("/{gameId}")
    public GameDto getGame(@PathVariable("gameId") UUID gameId) {
        return gameService.getGame(gameId);
    }

    @GetMapping("/{gameId}/legal-moves")
    public List<MoveDto> getLegalMoves(@PathVariable("gameId") UUID gameId) {
        return gameService.getLegalMoves(gameId);
    }

    @PostMapping("/{gameId}/moves")
    public GameDto playMove(@PathVariable("gameId") UUID gameId, @RequestBody(required = false) MoveRequest request) {
        return gameService.playMove(gameId, request);
    }

    @PostMapping("/{gameId}/draw")
    public GameDto claimDraw(@PathVariable("gameId") UUID gameId) {
        return gameService.claimDraw(gameId);
    }

    @PostMapping("/{gameId}/resign")
    public GameDto resign(@PathVariable("gameId") UUID gameId) {
        return gameService.resign(gameId);
    }

    @GetMapping("/{gameId}/moves")
    public List<MoveDto> getMoveHistory(@PathVariable("gameId") UUID gameId) {
        return gameService.getMoveHistory(gameId);
    }
}
