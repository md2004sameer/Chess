# Chess — Application Class Diagram

This diagram focuses on relationships across the REST API, game-session service, domain model, and errors. For the internals of move validation and chess rules, see the [Chess Engine Class Diagram](chess-engine-class-diagram.md). For the system layers, see [System Architecture](architecture.md).

```mermaid
classDiagram
    direction TB

    class ChessApplication {
        +main(args)
    }

    class GameController {
        +createGame()
        +getGame(gameId)
        +getLegalMoves(gameId)
        +playMove(gameId, request)
        +claimDraw(gameId)
        +resign(gameId)
        +getMoveHistory(gameId)
    }

    class ApiCorsConfiguration

    class GameService {
        -ConcurrentMap games
        +createGame()
        +getGame(gameId)
        +getLegalMoves(gameId)
        +playMove(gameId, request)
        +claimDraw(gameId)
        +resign(gameId)
        +getMoveHistory(gameId)
    }

    class GameSession {
        -Game game
    }

    class MoveRequest {
        <<record>>
        +String from
        +String to
        +String promotion
    }

    class GameDto {
        <<record>>
        +String gameId
        +String status
        +String activeColor
        +List board
        +int moveCount
        +boolean canClaimDraw
        +List sanHistory
    }

    class MoveDto {
        <<record>>
        +String from
        +String to
        +String promotion
    }

    class PieceDto {
        <<record>>
        +String color
        +String type
        +String symbol
    }

    class ApiExceptionHandler
    class ApiException {
        <<abstract>>
        -HttpStatus status
        -String code
    }
    class GameNotFoundException
    class GameConflictException
    class IllegalMoveException
    class InvalidRequestException
    class ApiErrorDto {
        <<record>>
        +String code
        +String message
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
        -squares
        +getPiece(position)
        +setPiece(position, piece)
        +copy()
    }

    class Piece {
        +Color color
        +PieceType type
    }

    class Move {
        +Position from
        +Position to
        +PieceType promotionType
    }

    class Position
    class Color
    class PieceType
    class CastlingRights
    class GameStatus
    class MoveResult
    class ValidationResult

    ChessApplication ..> GameController : Spring Boot host
    GameController --> GameService
    ApiCorsConfiguration ..> GameController : configures CORS
    GameController ..> MoveRequest : accepts
    GameController ..> GameDto : returns
    GameController ..> MoveDto : returns legal moves and history

    GameService o--> GameSession : registry by UUID
    GameSession *-- Game : owns one game
    GameService ..> MoveRequest : parses
    GameService ..> GameDto : builds snapshot
    GameService ..> MoveDto : maps moves
    GameService ..> PieceDto : maps pieces

    ApiExceptionHandler ..> ApiException : handles
    ApiExceptionHandler ..> ApiErrorDto : returns
    ApiException <|-- GameNotFoundException
    ApiException <|-- GameConflictException
    ApiException <|-- IllegalMoveException
    ApiException <|-- InvalidRequestException

    Game *-- GameState
    Game ..> MoveResult : returns
    Game ..> Move : records
    GameState *-- Board
    GameState --> Color
    GameState --> CastlingRights
    GameState --> Position : en passant target
    GameState --> GameStatus
    Board o--> Piece : stores pieces
    Piece --> Color
    Piece --> PieceType
    Move --> Position : from and to
    Move --> PieceType : promotion
    ValidationResult ..> Move : validates
```

## Reading the diagram

- **API and service:** `GameController` exposes HTTP routes; `GameService` owns the UUID-keyed registry and maps engine values to API DTOs.
- **One game per session:** every `GameSession` owns one `Game`. A session is synchronized when the service reads or changes that game.
- **Game state:** `Game` coordinates the rules and move history. `GameState` holds a position and its metadata; `Board` stores pieces but does not decide move legality.
- **Errors:** API-specific exceptions inherit from `ApiException` and are converted to consistent error responses.

## View in an editor

GitHub renders the Mermaid class diagram automatically. In VS Code, open this file and use `Cmd+Shift+V` on macOS or `Ctrl+Shift+V` on Windows/Linux.
