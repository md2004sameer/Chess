# Chess Engine — Architecture

This document maps the current `main` branch of [md2004sameer/Chess](https://github.com/md2004sameer/Chess). It covers the web application, REST API, game-session management, chess domain, move-generation pipeline, and move lifecycle.

## 1. System architecture

```mermaid
flowchart TB
    Player([Player])

    subgraph Client["Browser client"]
        HTML["index.html"]
        CSS["styles.css"]
        JS["app.js<br/>Board rendering, interaction, API calls"]
        HTML --- CSS
        HTML --> JS
    end

    subgraph Runtime["Spring Boot application"]
        Host["ChessApplication<br/>Embedded web server"]

        subgraph Web["HTTP / API layer"]
            Controller["GameController<br/>/api/games"]
            DTO["Request and response DTOs"]
            Errors["API exception handler<br/>Stable HTTP error responses"]
            Cors["ApiCorsConfiguration"]
        end

        subgraph Application["Application layer"]
            Service["GameService"]
            Sessions[("ConcurrentMap<UUID, GameSession><br/>In-memory registry")]
            Session["GameSession<br/>One synchronized session per game"]
        end

        subgraph Domain["Chess domain"]
            Game["Game<br/>Rules orchestration, history, SAN, repetition"]
            State["GameState<br/>Board + turn + metadata + status"]
            Rules["Draw-claim rules<br/>FiftyMoveRule / ThreefoldRepetitionRule"]
        end

        subgraph Engine["Rules engine"]
            Validator["MoveValidator"]
            Collector["LegalMoveCollector"]
            Castle["CastlingChecker"]
            Attack["AttackDetector"]
            Executor["MoveExecutor"]
            Status["GameStatusEvaluator"]
            Generator["MoveGenerator"]
            Factory["MoveGeneratorFactory"]
            PieceGenerators["PieceMoveGenerator implementations<br/>King, Queen, Rook, Bishop, Knight, Pawn"]
        end

        subgraph Core["Board and value model"]
            Board["Board"]
            Values["Piece, Position, Move, Color,<br/>PieceType, CastlingRights, GameStatus,<br/>ValidationResult"]
        end
    end

    Player --> HTML
    Host -->|serves static resources| Client
    JS <-->|HTTP + JSON| Controller
    Controller --> Service
    Controller --> DTO
    Controller --> Errors
    Cors -. configures .-> Controller

    Service --> Sessions
    Sessions --> Session
    Session --> Game
    Game --> State
    Game --> Rules

    Game --> Validator
    Game --> Executor
    Game --> Status
    Validator --> Collector
    Validator --> Generator
    Validator --> Castle
    Validator --> Attack
    Validator --> Executor
    Status --> Validator
    Status --> Attack
    Generator --> Factory
    Factory --> PieceGenerators

    State --> Board
    State --> Values
    Board --> Values
```

### Layer responsibilities

| Layer | Main components | Responsibility |
|---|---|---|
| Browser | `index.html`, `styles.css`, `app.js` | Render the board, handle player input, show legal moves/status/history, and call the API |
| HTTP API | `GameController`, DTOs, API error classes | Expose REST endpoints and translate HTTP requests/responses |
| Application | `GameService`, `GameSession` | Find/create independent games, validate request fields, map engine values to DTOs, and synchronize access per game |
| Domain | `Game`, `GameState`, draw-rule classes | Own a match's state and history; coordinate validation, execution, notation, repetition, draw claims, and game status |
| Rules engine | `MoveValidator`, `MoveExecutor`, `AttackDetector`, `GameStatusEvaluator` | Validate legal moves, apply moves, detect attacks/check, and evaluate terminal positions |
| Move generation | `MoveGenerator`, `MoveGeneratorFactory`, piece generators | Generate pseudo-legal movement patterns for each piece type |
| Core model | `Board` and `model` package | Store the board and represent pieces, moves, positions, colors, castling rights, and status |

## 2. Move lifecycle

This flow shows how `POST /api/games/{gameId}/moves` travels through the browser, API, game service, and chess engine.

```mermaid
sequenceDiagram
    participant Player
    participant Browser
    participant Controller as GameController
    participant Service as GameService
    participant Game
    participant Validator as MoveValidator
    participant Executor as MoveExecutor
    participant Evaluator as GameStatusEvaluator

    Player->>Browser: Submit a move
    Browser->>Controller: POST move request
    Controller->>Service: playMove(gameId, request)
    Service->>Service: Parse move and find game session
    Service->>Game: play(move)
    Game->>Validator: validate(state, move)
    Validator->>Executor: Simulate candidate on copied state
    Validator-->>Game: ValidationResult

    alt Move is illegal
        Game-->>Service: Rejected MoveResult
        Service-->>Controller: IllegalMoveException
        Controller-->>Browser: Error response
        Browser-->>Player: Show move error
    else Move is legal
        Game->>Executor: Apply move to live state
        Game->>Game: Record move, SAN, and repetition count
        Game->>Evaluator: Evaluate resulting position
        Evaluator-->>Game: GameStatus
        Game-->>Service: Accepted MoveResult
        Service-->>Controller: GameDto
        Controller-->>Browser: Updated game JSON
        Browser-->>Player: Render updated board
    end
```

King-safety validation simulates candidate moves against a copied game state. The live state is changed only after the move has passed validation.

## 3. Core class relationships

```mermaid
classDiagram
    direction TB

    class GameController {
        +createGame()
        +getGame(gameId)
        +getLegalMoves(gameId)
        +playMove(gameId, request)
        +claimDraw(gameId)
        +resign(gameId)
        +getMoveHistory(gameId)
    }

    class GameService {
        -ConcurrentMap games
        +createGame()
        +getGame(gameId)
        +playMove(gameId, request)
        +claimDraw(gameId)
        +resign(gameId)
        +getLegalMoves(gameId)
        +getMoveHistory(gameId)
    }

    class GameSession {
        -Game game
    }

    class Game {
        -GameState state
        -List history
        -List sanHistory
        -Map repetitionCounts
        +playMove(move)
        +legalMoves()
        +claimDraw()
        +resign()
        +status()
    }

    class GameState {
        -Board board
        -Color activeColor
        -CastlingRights castlingRights
        -Position enPassantTarget
        -int halfmoveClock
        -int fullmoveNumber
        -GameStatus status
        +copy()
    }

    class Board {
        +getPiece(position)
        +setPiece(position, piece)
        +copy()
    }

    class MoveValidator {
        +validate(state, move)
        +generateLegalMoves(state)
        +legalMovesFrom(state, position)
    }

    class MoveExecutor {
        +execute(state, move)
    }

    class GameStatusEvaluator {
        +evaluate(state, repetitionCount)
    }

    class AttackDetector {
        +isSquareAttacked(board, position, color)
        +isInCheck(board, color)
    }

    class CastlingChecker
    class LegalMoveCollector
    class MoveGenerator
    class MoveGeneratorFactory
    class PieceMoveGenerator {
        <<interface>>
        +generate(state, from)
    }

    class KingMoveGenerator
    class QueenMoveGenerator
    class RookMoveGenerator
    class BishopMoveGenerator
    class KnightMoveGenerator
    class PawnMoveGenerator

    GameController --> GameService
    GameService --> GameSession : indexes by UUID
    GameSession *-- Game
    Game *-- GameState
    GameState *-- Board
    Game --> MoveValidator
    Game --> MoveExecutor
    Game --> GameStatusEvaluator
    MoveValidator --> MoveGenerator
    MoveValidator --> MoveExecutor : simulate candidates
    MoveValidator --> AttackDetector
    MoveValidator --> CastlingChecker
    MoveValidator --> LegalMoveCollector
    GameStatusEvaluator --> MoveValidator
    GameStatusEvaluator --> AttackDetector
    MoveGenerator --> MoveGeneratorFactory
    MoveGeneratorFactory --> PieceMoveGenerator
    PieceMoveGenerator <|.. KingMoveGenerator
    PieceMoveGenerator <|.. QueenMoveGenerator
    PieceMoveGenerator <|.. RookMoveGenerator
    PieceMoveGenerator <|.. BishopMoveGenerator
    PieceMoveGenerator <|.. KnightMoveGenerator
    PieceMoveGenerator <|.. PawnMoveGenerator
```

## 4. Concurrency and persistence

- **Independent games:** `GameService` assigns each game a UUID and stores a separate `GameSession` in a `ConcurrentMap`.
- **Per-game synchronization:** service operations synchronize on the matching session, preventing competing requests from advancing the same game simultaneously.
- **In-memory only:** active games and move histories are not persisted to a database. Restarting the application clears the game registry and active games.
- **No chess AI:** the engine enforces chess rules and reports legal moves; it does not choose strategic moves for a player.

## 5. Tests and deployment

- Tests are grouped around API contracts, controller/service behavior, move validation, move generation, game status and lifecycle, draw rules, notation, and board behavior. Perft tests exercise move-generation correctness against known positions.
- The Spring Boot application serves the frontend from `src/main/resources/static`; there is no separate frontend build step.
- `Dockerfile` and `compose.yaml` support containerized execution.

## View the diagrams

- **GitHub:** open this file; Mermaid diagrams render automatically.
- **VS Code:** open the Markdown file and press `Cmd+Shift+V` (macOS) or `Ctrl+Shift+V` (Windows/Linux). Use `Cmd+K V` / `Ctrl+K V` to open the preview beside the source.
