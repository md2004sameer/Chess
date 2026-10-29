# Chess Engine — System Architecture

This diagram shows how the browser, Spring Boot API, game-session management, and chess rules engine connect. It is intentionally limited to the system structure; class relationships are documented separately in [Class Diagram](class-diagram.md) and [Chess Engine Class Diagram](chess-engine-class-diagram.md).

```mermaid
flowchart TB
    Player([Player])

    subgraph Browser["Browser client"]
        UI["index.html"]
        JS["app.js"]
        CSS["styles.css"]
        UI --> JS
        UI --- CSS
    end

    subgraph Spring["Spring Boot application"]
        Host["ChessApplication"]

        subgraph API["HTTP API layer"]
            Controller["GameController"]
            DTO["Request and response DTOs"]
            Errors["ApiExceptionHandler"]
            CORS["ApiCorsConfiguration"]
        end

        subgraph App["Application layer"]
            Service["GameService"]
            Registry[("ConcurrentMap keyed by UUID")]
            Session["GameSession<br/>Synchronized per game"]
        end

        subgraph Chess["Chess domain and rules engine"]
            Game["Game"]
            State["GameState"]
            Validator["MoveValidator"]
            Executor["MoveExecutor"]
            Evaluator["GameStatusEvaluator"]
            Generator["MoveGenerator"]
            Attack["AttackDetector"]
        end

        Board["Board"]
        Model["Chess model values"]
    end

    Player --> UI
    Host -->|"serves static files"| Browser
    JS <-->|"HTTP / JSON"| Controller
    Controller --> Service
    Controller -.-> DTO
    Errors -.-> Controller
    CORS -.-> Controller

    Service --> Registry
    Registry --> Session
    Session --> Game

    Game --> State
    Game --> Validator
    Game --> Executor
    Game --> Evaluator
    Validator --> Generator
    Validator --> Attack
    Evaluator --> Attack
    Evaluator --> Validator

    State --> Board
    Board --> Model

    classDef client fill:#eaf2ff,stroke:#4c78a8,color:#172b4d
    classDef api fill:#f3e8ff,stroke:#8956a3,color:#332044
    classDef app fill:#e9f7ef,stroke:#39845a,color:#153b25
    classDef engine fill:#fff3dc,stroke:#b7791f,color:#55370c

    class Player,UI,JS,CSS client
    class Controller,DTO,Errors,CORS api
    class Service,Registry,Session,Host app
    class Game,State,Validator,Executor,Evaluator,Generator,Attack,Board,Model engine
```

## Key design facts

- Each game is stored under its own UUID in an in-memory registry.
- Service operations synchronize on the matching game session so concurrent requests do not advance the same game at once.
- The Spring Boot application serves both the static browser UI and the REST API.
- Active games are not stored in a database. Restarting the application clears all current game sessions.
- The chess engine enforces rules; it does not select strategic moves like a chess AI.

## View in an editor

GitHub renders this Mermaid diagram when the file is opened. In VS Code, open this file and use `Cmd+Shift+V` on macOS or `Ctrl+Shift+V` on Windows/Linux to preview it.
