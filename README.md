# Chess Engine

A Java 21 chess rules engine with a console demo. The engine models a single game, generates pseudo-legal moves, validates king safety and special moves, applies accepted moves, and evaluates check, checkmate, stalemate, and supported draw conditions.

`ChessApplication` is the Spring Boot host entry point. Chess rules live in `com.sameer.chess` subpackages and do not import Spring. `ChessEngine` is a separate console entry point for interactive play.

## Package structure

| Class | Job | Pattern / principle |
|---|---|---|
| `ChessApplication` | Starts the Spring Boot host only. | Framework boundary |
| `model.Color` | Represents a side and its movement directions. | Enum value |
| `model.PieceType` | Represents a piece kind and promotion symbols. | Enum value |
| `model.Piece` | Holds a piece's color and type. | Immutable record |
| `model.Position` | Represents and parses a board square. | Immutable value |
| `model.Move` | Describes a move and optional promotion. | Immutable record |
| `model.CastlingRights` | Tracks remaining castling options. | Game metadata |
| `model.GameStatus` | Represents active, check, terminal, and draw states. | Enum state |
| `model.ValidationResult` | Reports whether a move is valid and why it was rejected. | Result value |
| `board.Board` | Stores pieces on an 8x8 grid and supports independent copies. | Rule-free state |
| `game.GameState` | Holds the board, side to move, rights, counters, and status. | Position state |
| `game.Game` | Owns one game lifecycle, history, repetition counts, and draw claims. | Facade / aggregate |
| `game.MoveResult` | Reports a move's acceptance and resulting status. | Result record |
| `game.DrawClaimRule` | Defines the draw-claim check used by the game. | Small strategy interface |
| `game.FiftyMoveRule` | Checks the 100-halfmove claim threshold. | Rule strategy |
| `game.ThreefoldRepetitionRule` | Checks the three-occurrence claim threshold. | Rule strategy |
| `game.ChessEngine` | Runs the optional interactive console game. | Console adapter |
| `api.GameController` | Exposes the REST game lifecycle and move endpoints. | HTTP adapter |
| `api.GameService` | Owns UUID-keyed in-memory games and serializes operations per game. | Application service |
| `api.dto.*` | Defines explicit request and response JSON values without exposing engine objects. | API contract |
| `api.error.*` | Maps expected client errors to stable HTTP status and error-code responses. | Error boundary |
| `api.ApiCorsConfiguration` | Enables CORS only for explicitly configured origins. | Host configuration |
| `movegen.PieceMoveGenerator` | Defines pseudo-legal generation for one piece type. | Strategy interface |
| `movegen.MoveGenerator` | Looks up a piece and delegates generation. | Dispatcher |
| `movegen.MoveGeneratorFactory` | Returns shared stateless generators by piece type. | Factory method |
| `movegen.SlidingMoves` | Shares ray traversal for sliding pieces. | Composition helper |
| `movegen.RookMoveGenerator` | Generates pseudo-legal rook moves. | Concrete strategy |
| `movegen.BishopMoveGenerator` | Generates pseudo-legal bishop moves. | Concrete strategy |
| `movegen.QueenMoveGenerator` | Generates pseudo-legal queen moves. | Concrete strategy |
| `movegen.KnightMoveGenerator` | Generates pseudo-legal knight moves. | Concrete strategy |
| `movegen.KingMoveGenerator` | Generates king moves and castling candidates. | Concrete strategy |
| `movegen.PawnMoveGenerator` | Generates pawn pushes, captures, promotions, and en passant candidates. | Concrete strategy |
| `engine.AttackDetector` | Detects attacked squares and check without move generation. | Independent rule query |
| `engine.MoveValidator` | Validates move coordinates, ownership, pattern, castling, and king safety. | Ordered validation |
| `engine.CastlingChecker` | Checks castling rights, path clearance, and attacked squares. | Focused rule checker |
| `engine.LegalMoveCollector` | Collects legal moves while avoiding repeated candidate generation. | Shared traversal |
| `engine.MoveExecutor` | Applies a move after validation. | State transition |
| `engine.GameStatusEvaluator` | Evaluates check, mate, stalemate, and automatic draws. | State evaluator |
| `util.BoardPrinter` | Produces a stable text representation for tests and debugging. | Pure formatter |
| `util.ConsoleRenderer` | Displays a game state in the console. | Console adapter |

## Playing a move

The core API is one `Game` instance. Construct it with `new Game()`, inspect legal moves with `getLegalMoves()`, and submit a parsed move with `playMove(Move.parse("e2e4"))`.

```text
Game.playMove(move)
  -> reject if game is over or move is null
  -> MoveValidator checks the move and king safety on a copy
  -> reject without changing game state when invalid
  -> MoveExecutor applies the validated move
  -> Game appends history and updates repetition counts
  -> GameStatusEvaluator sets the new position status
  -> return MoveResult
```

Use `Game.board()` or `Game.getState()` to inspect defensive snapshots. Mutating either a returned snapshot or the `GameState` supplied to the constructor does not modify the live game; submit moves through `Game.playMove()` to change its position.

## REST API

The Spring Boot host provides a REST API under `/api/games`. Each game has a UUID and is held in a process-local, thread-safe in-memory registry. Reads and writes for one game are serialized per game; unrelated games do not share a lock. API responses use DTOs and never serialize `Game`, `Board`, `GameState`, or other engine objects.

The same host serves a browser-based two-player interface at `/` from `src/main/resources/static/`. Its HTML, CSS, and vanilla JavaScript are kept in separate `index.html`, `styles.css`, and `app.js` files. The board, legal destinations, promotion choices, status, and move history come from this API; the browser does not implement chess rules. A game ID is kept in the current browser tab's session storage and is replaced with a new game if the server no longer recognizes it.

Squares use algebraic coordinates (`a1` through `h8`). A game snapshot has this shape:

```json
{
  "gameId": "7ad9d826-5b52-49d4-a25c-b3ff3b5f58b0",
  "status": "ACTIVE",
  "activeColor": "WHITE",
  "board": [
    [
      {"color": "BLACK", "type": "ROOK", "symbol": "r"},
      null
    ]
  ],
  "moveCount": 0
}
```

The abbreviated board above illustrates cell values: the actual response always contains 8 rows of 8 cells, with rank 8 first, rank 1 last, and file `a` first in each row. Empty squares are JSON `null`. Piece `color` and `type` are uppercase names; `symbol` is uppercase for White and lowercase for Black. `status` is one of `ACTIVE`, `CHECK`, `CHECKMATE`, `STALEMATE`, `DRAW_FIFTY_MOVE`, `DRAW_THREEFOLD_REPETITION`, `DRAW_INSUFFICIENT_MATERIAL`, `DRAW_SEVENTY_FIVE_MOVE`, or `DRAW_FIVEFOLD_REPETITION`.

### Endpoints

| Method and path | Success | Contract |
|---|---:|---|
| `POST /api/games` | `201 Created` | Creates a game. The response is a game snapshot, and `Location` is `/api/games/{gameId}`. |
| `GET /api/games/{gameId}` | `200 OK` | Returns the current game snapshot: `gameId`, `status`, `activeColor`, `board`, and `moveCount`. |
| `GET /api/games/{gameId}/legal-moves` | `200 OK` | Returns a JSON array of legal move DTOs for the active side. The array is empty after a terminal result. |
| `POST /api/games/{gameId}/moves` | `200 OK` | Plays a move and returns the resulting game snapshot. |
| `GET /api/games/{gameId}/moves` | `200 OK` | Returns accepted moves in game history as a JSON array of move DTOs. |

A move request has required `from` and `to` algebraic square strings and an optional one-character `promotion` selector (`q`, `r`, `b`, or `n`; case-insensitive). A move DTO has `from`, `to`, and `promotion`, where promotion is `null` for an ordinary move and the selected lowercase letter for a promotion. For example:

```sh
curl -i -X POST http://localhost:8080/api/games \
  -H 'Accept: application/json'
```

```sh
curl -X POST http://localhost:8080/api/games/7ad9d826-5b52-49d4-a25c-b3ff3b5f58b0/moves \
  -H 'Content-Type: application/json' \
  -d '{"from":"e2","to":"e4"}'
```

For a game positioned so a pawn can promote, include the selector:

```sh
curl -X POST http://localhost:8080/api/games/7ad9d826-5b52-49d4-a25c-b3ff3b5f58b0/moves \
  -H 'Content-Type: application/json' \
  -d '{"from":"e7","to":"e8","promotion":"q"}'
```

For accepted moves the response is the new game snapshot. Legal moves and history use the same move DTO shape, for example:

```json
[
  {"from": "e2", "to": "e4", "promotion": null}
]
```

The legal-moves endpoint returns all current legal moves, not just the one shown in this abbreviated example. A syntactically valid move rejected by the engine returns `422 Unprocessable Content` with `{"code":"ILLEGAL_MOVE","message":"..."}`. This includes a missing promotion on a pawn's last-rank move, so the engine's validation message is retained. Malformed JSON, coordinates, or promotion selectors return `400 Bad Request` with code `INVALID_REQUEST`; an invalid UUID path value is also `400`. A valid but unknown UUID returns `404 Not Found` with code `GAME_NOT_FOUND`. A move submitted after the game ends returns `409 Conflict` with code `GAME_FINISHED`. Errors use the stable shape `{"code":"...","message":"..."}`.

### Local frontend integration and CORS

Start the API with `./gradlew bootRun`; it listens on port 8080 by default. A frontend served from the same origin can call the API without CORS configuration. For a frontend served from another origin, configure explicit origins using a comma-separated environment variable; no origins are allowed by default:

```sh
CHESS_CORS_ALLOWED_ORIGINS=http://localhost:3000 ./gradlew bootRun
```

### Docker deployment

Build and start the application with Docker Compose:

```sh
docker compose up --build -d
```

The web app and API are available at `http://localhost:8080`. To use a different host port, set `CHESS_PORT` before starting Compose:

```sh
CHESS_PORT=8081 docker compose up --build -d
```

If a separate frontend needs cross-origin API access, set `CHESS_CORS_ALLOWED_ORIGINS` to its origin (or a comma-separated list of origins). The default allows no cross-origin requests:

```sh
CHESS_CORS_ALLOWED_ORIGINS=https://chess.example.com docker compose up --build -d
```

View container logs with `docker compose logs -f chess`, and stop the service with `docker compose down`. Game state is process-local, so run a single instance unless you add shared persistent storage.

A browser client can create a game and submit a move with ordinary `fetch` calls:

```js
const response = await fetch("http://localhost:8080/api/games", { method: "POST" });
const game = await response.json();
const moved = await fetch(`http://localhost:8080/api/games/${game.gameId}/moves`, {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ from: "e2", to: "e4" })
});
const updatedGame = await moved.json();
```

Games are not persisted: all UUIDs and history are lost when the application process restarts. The API does not provide authentication, multiplayer coordination, or AI.

## Build and tests

The project uses the Gradle wrapper and Java 21:

```sh
./gradlew cleanTest test
./gradlew test
./gradlew check
```

The suite includes per-piece move-generation tests, validator and castling tests, lifecycle and draw tests, and standard Perft reference positions. Perft checks include the initial position (20, 400, 8,902), Kiwipete (48, 2,039, 97,862), and standard endgame, promotion, and tactical positions with reference counts through depths 3 or 4.

To start the Spring Boot host, run `./gradlew bootRun`. The console demo is separate and can be launched from the `com.sameer.chess.game.ChessEngine` main class in an IDE.

## Known limitations

- Insufficient-material detection handles bare kings, a single minor piece, and bishop-only positions where all bishops occupy the same square color. It is conservative for other multiple-minor-piece combinations and does not label them insufficient merely because a side cannot force mate.
- `Game.getState()` and `Game.board()` return defensive snapshots; the game changes only through validated operations.
- The REST game registry is in-memory and process-local; games do not survive an application restart.

## Next steps

Not implemented:

- FEN/PGN import and export, chess clocks, resignation, undo, and additional draw rules such as repetition adjudication beyond the implemented claim/automatic thresholds.
- Persistence, WebSockets, matchmaking, or authentication.
- Chess AI or a graphical user interface.
