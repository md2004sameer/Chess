# Chess Engine — Game and Rules Class Diagram

This diagram isolates the chess rules engine from the Spring Boot API and browser. It follows the code under `com.sameer.chess.game`, `engine`, `movegen`, `board`, and `model`.

For the overall application layers, see [System Architecture](architecture.md). For the API and application classes, see [Application Class Diagram](class-diagram.md).

```mermaid
classDiagram
    direction TB

    class Game {
        -GameState state
        -MoveGenerator moveGenerator
        -AttackDetector attackDetector
        -MoveExecutor moveExecutor
        -MoveValidator moveValidator
        -GameStatusEvaluator statusEvaluator
        -FiftyMoveRule fiftyMoveRule
        -ThreefoldRepetitionRule threefoldRule
        -List history
        -List sanHistory
        -Map repetitionCounts
        +newGame()
        +getState()
        +getHistory()
        +getSanHistory()
        +getLegalMoves()
        +playMove(move)
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
        +initial()
        +copy()
        +positionKey(includeEnPassantTarget)
    }

    class MoveResult {
        +boolean accepted
        +String message
        +GameStatus status
    }

    class DrawClaimRule {
        <<interface>>
        +canClaim(state, repetitions) boolean
    }

    class FiftyMoveRule {
        +canClaim(state, repetitions) boolean
    }

    class ThreefoldRepetitionRule {
        +canClaim(state, repetitions) boolean
    }

    class MoveValidator {
        -MoveGenerator generator
        -MoveExecutor executor
        -AttackDetector attackDetector
        -CastlingChecker castlingChecker
        -LegalMoveCollector legalMoveCollector
        +validate(state, move) ValidationResult
        +legalMoves(state)
        +legalMovesFrom(state, from)
        +hasAnyLegalMove(state)
    }

    class LegalMoveCollector {
        -MoveGenerator generator
        +all(state)
        +from(state, position)
        +hasAny(state)
    }

    class MoveExecutor {
        +execute(state, move)
    }

    class AttackDetector {
        +isSquareAttacked(board, target, attacker) boolean
        +findKing(board, color) Position
        +isInCheck(board, color) boolean
    }

    class CastlingChecker {
        -AttackDetector attackDetector
        +validate(state, move) ValidationResult
    }

    class GameStatusEvaluator {
        -MoveValidator validator
        -AttackDetector attackDetector
        +evaluate(state, repetitions) GameStatus
    }

    class MoveGenerator {
        +generatePseudoLegalMoves(state, from)
    }

    class MoveGeneratorFactory {
        +forPiece(type) PieceMoveGenerator
    }

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
    class SlidingMoves

    class Board {
        -squares
        +initialPosition()
        +getPiece(position)
        +setPiece(position, piece)
        +isEmpty(position)
        +copy()
    }

    class Piece
    class Position
    class Move
    class Color
    class PieceType
    class CastlingRights
    class GameStatus
    class ValidationResult

    Game *-- GameState : owns live position
    Game *-- MoveValidator
    Game *-- MoveExecutor
    Game *-- GameStatusEvaluator
    Game *-- MoveGenerator
    Game *-- AttackDetector
    Game *-- FiftyMoveRule
    Game *-- ThreefoldRepetitionRule
    Game ..> MoveResult : returns
    Game ..> Move : records accepted moves

    GameState *-- Board
    GameState --> Color : active side
    GameState --> CastlingRights
    GameState --> Position : en passant target
    GameState --> GameStatus
    Board o--> Piece : stores on squares
    Board ..> Position : square coordinates
    Piece --> Color
    Piece --> PieceType
    Move --> Position : from and to
    Move --> PieceType : optional promotion

    DrawClaimRule <|.. FiftyMoveRule
    DrawClaimRule <|.. ThreefoldRepetitionRule

    MoveValidator --> MoveGenerator
    MoveValidator --> MoveExecutor : simulates candidates on copied state
    MoveValidator --> AttackDetector : king safety
    MoveValidator --> CastlingChecker
    MoveValidator --> LegalMoveCollector
    CastlingChecker --> AttackDetector
    LegalMoveCollector --> MoveGenerator
    GameStatusEvaluator --> MoveValidator : any legal moves
    GameStatusEvaluator --> AttackDetector : check detection

    MoveGenerator --> MoveGeneratorFactory
    MoveGeneratorFactory --> PieceMoveGenerator : selects by piece type
    PieceMoveGenerator <|.. KingMoveGenerator
    PieceMoveGenerator <|.. QueenMoveGenerator
    PieceMoveGenerator <|.. RookMoveGenerator
    PieceMoveGenerator <|.. BishopMoveGenerator
    PieceMoveGenerator <|.. KnightMoveGenerator
    PieceMoveGenerator <|.. PawnMoveGenerator
    QueenMoveGenerator ..> SlidingMoves
    RookMoveGenerator ..> SlidingMoves
    BishopMoveGenerator ..> SlidingMoves
```

## The game flow at a glance

1. `Game.playMove()` asks `MoveValidator` to validate the requested move.
2. `MoveValidator` checks coordinates, ownership and turn, promotion, movement patterns, castling, and king safety.
3. For king-safety checks, candidate moves are applied to a copied `GameState`; the live game is not changed by a rejected move.
4. After validation succeeds, `MoveExecutor` updates the live state, including the board, active side, castling rights, en-passant target, and move counters.
5. `Game` records move history and SAN notation, updates repetition counts, and calls `GameStatusEvaluator`.
6. `GameStatusEvaluator` checks for legal moves, check, checkmate, stalemate, and automatic draw conditions. Draw claims for the fifty-move and threefold-repetition rules are handled by their rule classes.

## Design boundaries

- `Board` stores pieces; it does not determine whether a move is legal.
- `MoveGenerator` produces pseudo-legal moves. `MoveValidator` filters candidates using the full rules and king-safety checks.
- `MoveExecutor` applies a move after validation. Callers should use `Game.playMove()`, not execute moves directly.
- `GameState.copy()` and board copies support safe simulation and read-only snapshots.
- The engine has no dependency on Spring MVC, HTTP DTOs, or the browser UI.

## View in an editor

GitHub renders the Mermaid class diagram automatically. In VS Code, open this file and use `Cmd+Shift+V` on macOS or `Ctrl+Shift+V` on Windows/Linux.
