# Architecture Diagram

This project is an in-process chess engine packaged as a Spring Boot application. Its core exposes game, board, move generation, and validation components; the repository does not currently define a user interface, HTTP controllers, or persistent storage.

## Application Architecture

<!-- mermaid-checked: no \n, no em-dash/en-dash, no {} in labels, subgraphs are id["label"], arrows are -->|"label"|, all subgraphs closed by end, ids unique -->
```mermaid
flowchart TD
    subgraph CallerLayer["Caller"]
        Caller["In-process game client"]
    end
    subgraph RuntimeLayer["Application Runtime"]
        Runtime["Spring Boot runtime"]
        Bootstrap["ChessApplication"]
        Config["Spring bean configuration"]
    end
    subgraph ChessLayer["Chess Domain"]
        GameApi["Game orchestration"]
        Validation["Move validation"]
        Generation["Piece move generation"]
        SpecialRules["Special move rules"]
        Execution["Move execution and undo"]
    end
    subgraph StateLayer["In-memory State"]
        Board["Mutable chess board"]
        GameState["Turn and rule state"]
        Model["Pieces, positions, and moves"]
    end

    Caller -->|"uses game operations"| GameApi
    Runtime -->|"starts"| Bootstrap
    Bootstrap -->|"loads"| Config
    Config -->|"wires validation services"| Validation
    GameApi -->|"requests legal moves"| Validation
    GameApi -->|"plays and undoes moves"| Execution
    GameApi -->|"tracks turn and history"| GameState
    GameApi -->|"owns current position"| Board
    Validation -->|"requests candidates"| Generation
    Validation -->|"applies special cases"| SpecialRules
    Validation -->|"simulates candidate moves"| Execution
    Generation -->|"reads placement"| Board
    SpecialRules -->|"reads board and rule context"| GameState
    Execution -->|"updates placement"| Board
    Board -->|"stores"| Model
    GameState -->|"records"| Model
```

### Technology Stack Summary

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Language and runtime | Java | 21 | Implements the chess engine and application entry point |
| Application framework | Spring Boot | 4.1.1 | Application startup and dependency configuration |
| Web framework dependency | Spring MVC starter | Managed by Spring Boot 4.1.1 | Included in the build; no HTTP controller or web endpoint is currently defined |
| Build | Gradle Wrapper | 9.8.1 | Compiles the project and runs its JUnit Platform tests |
| Domain | Project Java packages | Not applicable | Game state, board operations, move generation, validation, and execution |

### Data Storage & External Services

The board and game state are held in memory for each `Game` instance. The project has no database, cache, message broker, or external API integration; move history is maintained in memory and supports undo. Although the web MVC starter is a build dependency, no HTTP-facing components are present in the inspected source.

### Key Architectural Decisions

- Board read and write capabilities are separated into `BoardReader` and `BoardWriter`, combined by `MutableBoard`.
- Move generation is selected by piece type through a registry, while castling, en passant, and promotion are added through special-move rules.
- Legal-move validation simulates candidate moves on the mutable board and undoes them to check king safety; the game separately records state snapshots for game-level undo.

## Component Relationships

<!-- mermaid-checked: no \n, no em-dash/en-dash, no {} in labels, subgraphs are id["label"], arrows are -->|"label"|, all subgraphs closed by end, ids unique -->
```mermaid
flowchart LR
    subgraph SetupLayer["Application Setup"]
        cApp["ChessApplication"]
        cChessConfig["ChessConfiguration"]
        cGeneratorConfig["MoveGeneratorConfiguration"]
    end
    subgraph GameLayer["Game Orchestration"]
        cGame["Game"]
        cGameState["GameState"]
    end
    subgraph RuleLayer["Validation and Move Rules"]
        cValidator["MoveValidator"]
        cAttack["AttackDetector"]
        cRegistry["MoveGeneratorRegistry"]
        cGenerators["Piece move generators"]
        cSpecial["Castling, en passant, promotion"]
    end
    subgraph DomainLayer["Board and Domain"]
        cMutableBoard["MutableBoard"]
        cBoard["Board"]
        cExecutor["MoveExecutor"]
        cMove["Move"]
        cModel["Piece and Position"]
    end

    cApp -->|"starts Spring context"| cChessConfig
    cGeneratorConfig -->|"registers generators"| cRegistry
    cChessConfig -->|"constructs validator"| cValidator
    cGame -->|"requests legal moves"| cValidator
    cGame -->|"executes and reverses moves"| cExecutor
    cGame -->|"updates turn and history"| cGameState
    cGame -->|"reads and mutates"| cMutableBoard
    cValidator -->|"selects by piece type"| cRegistry
    cValidator -->|"adds special candidates"| cSpecial
    cValidator -->|"checks attacked squares"| cAttack
    cValidator -->|"simulates and undoes moves"| cExecutor
    cRegistry -->|"dispatches to"| cGenerators
    cGenerators -->|"reads placement"| cMutableBoard
    cSpecial -->|"uses rule context"| cGameState
    cSpecial -->|"reads board"| cMutableBoard
    cExecutor -->|"applies moves"| cMutableBoard
    cExecutor -->|"creates executed move records"| cMove
    cBoard -->|"implements"| cMutableBoard
    cBoard -->|"stores"| cModel
    cMove -->|"uses positions"| cModel
```

### Component Inventory

| Component | Layer | Type | Responsibility |
|---|---|---|---|
| `ChessApplication` | Application setup | Spring Boot entry point | Starts the Spring application context |
| `ChessConfiguration` | Application setup | Spring configuration | Provides move execution, attack detection, special rules, and the validator |
| `MoveGeneratorConfiguration` | Application setup | Spring configuration | Registers piece-specific move generators |
| `Game` | Game orchestration | Domain service | Coordinates legal-move queries, play, history, and undo |
| `GameState` | Game orchestration | Domain state and rule context | Tracks the active side, castling rights, en passant, move counters, and move history |
| `MoveValidator` | Validation and move rules | Domain service | Builds pseudo-legal candidates and filters them for chess legality and king safety |
| `AttackDetector` | Validation and move rules | Domain service | Determines whether a square is attacked |
| `MoveGeneratorRegistry` | Validation and move rules | Strategy registry | Selects the generator for a piece type |
| Piece move generators | Validation and move rules | Strategy implementations | Generate normal candidate moves for each piece type |
| Castling, en passant, promotion rules | Validation and move rules | Rule strategies | Add or transform special-move candidates |
| `MutableBoard` | Board and domain | Board interface | Combines read-only placement queries and board mutations |
| `Board` | Board and domain | In-memory implementation | Stores the 8-by-8 piece placement and provides board operations |
| `MoveExecutor` | Board and domain | Domain service | Applies and reverses normal and special moves |
| `Move` | Board and domain | Domain value | Represents a move choice, including special move kind and promotion type |
| `Piece` and `Position` | Board and domain | Domain values | Represent chess pieces and validated board coordinates |
