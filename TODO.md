# Chess Engine Build Checklist

## Step 0: Inspect the repo
- [x] Note the base package, Java version, Spring Boot version, and Maven/Gradle setup.
- [x] List existing chess classes, tests, and the application entry point.
- [x] Run the existing tests and record the baseline result.
- [x] Create the `chess-engine` branch.

Baseline: `./gradlew test` failed during `:compileJava` because `MoveValidator` references the missing `StandardMoveRules` class. No tests ran. Repository setup: `com.sameer.chess`, Java 21, Spring Boot 4.1.1, Gradle 9.8.1 wrapper. Existing sources are in `src/main/java/com/sameer/chess`; tests are in `src/test/java/com/sameer/chess/ChessEngineTests.java`; `ChessApplication.main()` starts Spring Boot and `ChessEngine.main()` is a separate console entry point.

## Phase 1: `model/` package
- [x] Add `Color` (`WHITE`, `BLACK`) with `opposite()`.
- [x] Add `PieceType` (`KING`, `QUEEN`, `ROOK`, `BISHOP`, `KNIGHT`, `PAWN`).
- [x] Add `Piece(Color color, PieceType type)` record.
- [x] Add `Position(int row, int col)` with parsing, formatting, and bounds checking. Row 0 is rank 8; row 7 is rank 1.
- [x] Add `Move(Position from, Position to, PieceType promotion)` record, with optional (`null`) promotion.
- [x] Add `CastlingRights`, `GameStatus` (`IN_PROGRESS`, `CHECK`, `CHECKMATE`, `STALEMATE`), and `ValidationResult(boolean valid, String message)`.
- [x] Add tests for valid/invalid position parsing, move equality, and `Color.opposite()`.
- [x] Run tests, review, and commit.

Phase 1 note: Added the requested value types under `com.sameer.chess.model`. The full-suite baseline compile blocker was the missing `StandardMoveRules` factory; it has been restored so the repository builds and the existing suite can run. `./gradlew test` passes all 27 tests (23 existing and 4 new).

## Package organization task (added 2026-10-21)
- [x] Inspect base package and build tool: `com.sameer.chess`, Gradle.
- [x] Run and record a fresh baseline build and test run.
- [x] Move/split existing chess classes into the requested `model`, `board`, `game`, `movegen`, `engine`, and `util` packages without changing chess logic.
- [x] Move and organize tests under matching test packages.
- [x] Add minimal TODO stubs for requested classes that do not exist, with one-line Javadocs.
- [x] Remove duplicates only after compilation succeeds; ensure no Spring imports in chess packages.
- [x] Run build and all tests, compare results with baseline, and print the final source tree.

Package-organization baseline: `./gradlew test` — `BUILD SUCCESSFUL`; Gradle reported tasks `UP-TO-DATE`, and the prior report contained 27 tests with zero failures/errors. Post-organization `./gradlew test` — `BUILD SUCCESSFUL`, 27 tests, zero failures/errors/skips.

Organization note: Existing move logic remains in `MoveGenerator`; requested per-piece generators, `SlidingMoves`, `MoveGeneratorRegistry`, and `BoardPrinter` were absent and are TODO stubs because splitting/implementing them would change or add behavior.

## Phase 2: Board, GameState, BoardPrinter
- [ ] Add an 8x8 `Board` with `getPiece`, `setPiece`, `isInside`, and `isEmpty`. Board does not handle turns, legality, check, or status.
- [ ] Add `Board.copy()` and a factory for the initial position.
- [ ] Add `GameState` with board, active color, castling rights, en passant target, halfmove clock, fullmove number, status, and safe `copy()`.
- [ ] Add `BoardPrinter` for debugging and tests.
- [ ] Test initial piece placement (32 pieces) and independent copies.
- [ ] Run tests, review, and commit.

## Phase 3: `movegen/` package
- [ ] Add `MoveGenerator` interface: `List<Move> generate(GameState state, Position from)`.
- [ ] Add shared `SlidingMoves` composition helper.
- [ ] Add rook, bishop, queen, knight, king, and pawn move generators.
- [ ] Include king castling candidates; validate their conditions later.
- [ ] Include pawn pushes, captures, promotions (Q/R/B/N), and en passant.
- [ ] Generate pseudo-legal moves only; never generate a capture of a king.
- [ ] Add `MoveGeneratorRegistry` mapping piece type to generator.
- [ ] Add a test class per generator, covering open/blocked paths, captures, and edges.
- [ ] Run tests, review, and commit.

## Phase 4: AttackDetector, MoveValidator, MoveExecutor
- [ ] Add `AttackDetector.isSquareAttacked(board, square, byColor)` and `isInCheck(board, color)` without using move generators; pawn attacks include empty diagonals.
- [ ] Add ordered validation: coordinates; piece and turn; promotion; movement pattern; castling; king safety.
- [ ] For castling, check rights, empty squares, and king safety in, through, and into check; remove the king from its square when testing sliding attacks.
- [ ] Add `legalMoves`, `legalMovesFrom`, and short-circuiting `hasAnyLegalMove`; generated moves need only castling and king-safety checks.
- [ ] Add a restricted `MoveExecutor` for validated moves, handling captures, castling, en passant, promotion, rights, counters, and turn switching.
- [ ] Test pinned pieces, king safety, check responses, castling conditions/rights, en passant and discovered check, all promotions, invalid promotion, and unchanged state after rejection.
- [ ] Run tests, review, and commit.

## Phase 5: GameStatusEvaluator and Game
- [ ] Add `GameStatusEvaluator` for in-progress, check, checkmate, and stalemate.
- [ ] Add one `Game` with `newGame`, `play`, legal move accessors, status, active color, and a read-only board view or copy.
- [ ] Reject moves cleanly when invalid or after the game ends.
- [ ] Test 20 starting legal moves, wrong-turn rejection, Fool's Mate, and a known stalemate.
- [ ] Run tests, review, and commit.

## Phase 6: Perft, clean-up, README
- [ ] Add perft tests: initial position depths 1, 2, and 3 equal 20, 400, and 8,902.
- [ ] Fix any perft-exposed bug without weakening assertions.
- [ ] Remove obsolete/duplicate code; confirm no Spring imports in chess packages and `ChessApplication.main()` only starts Spring Boot.
- [ ] Document purpose, package tree/class responsibilities/patterns, `Game.play()` flow, test instructions, limitations, and out-of-scope next steps in README.
- [ ] Run the full test suite, merge the branch, and tag the release.
