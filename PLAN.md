# Implementation Plan

## Execution rules

- Work through the steps in order; do not start a later step until `./gradlew test` passes for the current step.
- Create one clear commit per step. Do not use `Comparator` or custom comparison lambdas, and do not add chess features.
- Preserve passing behavior except where a listed fix requires a change. If a requested change proves ambiguous or would require an unrequested public API decision, stop and ask before implementing it.
- Some supplied line numbers are stale in this checkout. Locate targets by class and method name; no line-number-specific code edits are assumed.

## Step 1: Executor guards

- **Files touched:** `src/main/java/com/sameer/chess/move/MoveExecutor.java`; `src/test/java/com/sameer/chess/move/MoveExecutorTests.java`.
- **Tests to add or change:** Test same-square moves, friendly captures, king captures, and en passant with no captured piece; assert rejected moves leave all affected squares unchanged. The regression cases and guards are already present in this checkout, so inspect and reuse them rather than duplicating them. The original test-first failure state predates this plan; do not remove working guards merely to recreate it.
- **Risk:** Low. The requested behavior is already implemented. Confirm all four guards run before mutation and leave the board unchanged.
- **Verification:** `./gradlew test`
- **Commit:** Existing work is already committed; make no empty commit if no Step 1 code or test changes are needed.

## Step 2: Move generator registry

- **Files touched:** `src/main/java/com/sameer/chess/move/generator/MoveGeneratorRegistry.java`; `src/main/java/com/sameer/chess/move/generator/MoveGeneratorConfiguration.java`; `src/test/java/com/sameer/chess/move/generator/MoveGeneratorRegistryTests.java`.
- **Tests to add or change:** Update construction tests for `List<MoveGenerator>`; assert every `Type` resolves to a generator whose `type()` is that key; assert duplicate types throw.
- **Risk:** Medium. Registration will fail fast for duplicate types rather than silently overwriting a generator; configuration must supply exactly one generator per piece type.
- **Verification:** `./gradlew test`
- **Commit:** One commit for this step after tests pass.

## Step 3: Direction tables and shared generator base

- **Files touched:** Add `Direction.java`, package-private `Directions.java`, and the shared abstract generator base under `src/main/java/com/sameer/chess/move/generator/`; update `BishopMoveGenerator.java`, `RookMoveGenerator.java`, `QueenMoveGenerator.java`, `KingMoveGenerator.java`, `KnightMoveGenerator.java`, `SlidingMoveGenerator.java`, `StepMoveGenerator.java`, and `PawnMoveGenerator.java`; update `src/test/java/com/sameer/chess/move/generator/MoveGeneratorTests.java`.
- **Tests to add or change:** Add/retain coverage for a queen friendly blocker and enemy capture; black rook, bishop, knight, and king capture/friendly-block cases; white pawn blocked from its double step; and all existing generator behaviors. Check direction lists are immutable.
- **Risk:** Medium. Consolidating null/type and landing checks can change edge behavior if piece-specific rules are lost. Keep DIAGONAL, ORTHOGONAL, and ALL as immutable lists; Knight retains its own list. Standardize protected `directions()` visibility.
- **Verification:** `./gradlew test`
- **Commit:** One commit for this step after tests pass.

## Step 4: Executed moves, capture positions, and atomic undo

- **Files touched:** `src/main/java/com/sameer/chess/move/Move.java`; `src/main/java/com/sameer/chess/move/MoveExecutor.java`; `src/main/java/com/sameer/chess/game/Game.java`; `src/main/java/com/sameer/chess/game/GameState.java`; `src/main/java/com/sameer/chess/validation/MoveValidator.java`; `src/test/java/com/sameer/chess/move/MoveTests.java`; `src/test/java/com/sameer/chess/move/MoveExecutorTests.java`; any affected game and validator tests.
- **Tests to add or change:** Cover normal/en passant `capturePosition()`, the `(move, moved, captured)` result, promotion retaining the original pawn, and unchanged-board undo failures for wrong destination piece, occupied origin, occupied castling rook start, and occupied en passant capture square. Update every `undo` caller to `(board, executed)`. Add/use `Move.isPromotion()` and `is*` helpers instead of hand-written `kind()` comparisons.
- **Risk:** Medium. `Executed` and `undo` are public API changes explicitly requested here; migrate all callers together. Both execution and undo dispatch must be exhaustive over `MoveKind`. Validate every undo precondition before mutation.
- **Verification:** `./gradlew test`
- **Commit:** One commit for this step after tests pass.

## Step 5: Spring wiring and package-cycle removal

- **Files touched:** Remove component annotations from `MoveExecutor.java`, `AttackDetector.java`, `MoveValidator.java`, `CastlingRule.java`, `EnPassantRule.java`, and `PromotionRule.java`; add a configuration class for these beans and the ordered rule list; add `src/main/java/com/sameer/chess/move/rule/RuleContext.java`; update `SpecialMoveRule.java`, rule implementations, `GameState.java`, `src/test/java/com/sameer/chess/TestBoards.java`, and `src/test/java/com/sameer/chess/ChessApplicationTests.java`.
- **Tests to add or change:** Reuse the static rule-list factory in `TestBoards.validator()`; assert `ChessApplicationTests` can autowire a non-null `MoveValidator`; preserve and verify rule order Castling, EnPassant, Promotion.
- **Risk:** High. Explicit bean configuration affects startup and test construction. Keep constructor dependencies and rule order consistent; `move.rule` must no longer import or depend on `game`.
- **Verification:** `./gradlew test`
- **Commit:** One commit for this step after tests pass.

## Step 6: Model and board cleanup

- **Files touched:** `src/main/java/com/sameer/chess/model/Position.java`, `Piece.java`, and the color/type enums (renamed and moved from `model/enums` to `model`); `src/main/java/com/sameer/chess/board/Board.java`, `BoardReader.java`; `src/main/java/com/sameer/chess/move/Move.java`, `Castling.java`; `CastlingRule.java`; all production and test references to the renamed types and removed accessors; affected board, validator, and attack-detector tests.
- **Tests to add or change:** Use record accessors and assert null promotion metadata with `assertNull`; test derived promotion rows and promotion targets; update `findKing` tests for `Optional`; verify grid rendering, immutable occupied positions, `Position.SIZE` bounds, and unchanged board after failed `movePiece`. Remove tests and callers for deleted `copy()`/`isInBounds()`.
- **Risk:** High. Renaming/moving public model enums and changing `findKing` to `Optional` are broad API changes. Migrate every source/test reference; preserve the documented coordinate convention. Verify `toString()` uses uppercase white, lowercase black, and `N` for knights.
- **Verification:** `./gradlew test`
- **Commit:** One commit for this step after tests pass.

## Step 7: Remaining behavioral tests

- **Files touched:** `src/test/java/com/sameer/chess/move/rule/SpecialMoveRuleTests.java`; `MoveGeneratorTests.java`; `MoveTests.java`; `AttackDetectorTests.java`; `BoardTests.java`; `MoveExecutorTests.java`; `TestBoards.java`.
- **Tests to add or change:** Assert exact promotion targets, black promotion, and capture promotion. Add the specified generator cases; attack tests for knight, king, blocked bishop, queen, and white pawn; Move tests rejecting PAWN promotion and null `from`, `to`, or `kind`; split the first board test by behavior and assert failed `movePiece` leaves the board unchanged. Remove unused `GameState` setup/import from `MoveExecutorTests`; delegate coordinate-based `TestBoards.place()` to its `Position` overload.
- **Risk:** Medium. Broader tests may expose existing behavioral discrepancies; change production behavior only where required by the already specified fixes, not to add chess features.
- **Verification:** `./gradlew test`
- **Commit:** One commit for this step after tests pass.

## Step 8: Build and repository hygiene

- **Files touched:** `build.gradle`; add `.editorconfig` and `README.md`; convert indentation in `src/main/java/com/sameer/chess/ChessApplication.java` and `src/test/java/com/sameer/chess/ChessApplicationTests.java`.
- **Tests to add or change:** No additional behavioral tests are planned; verify the dependency replacement and full suite. README covers project purpose, Java 21, `./gradlew test`, package layout, and row/column convention.
- **Risk:** Medium. Dependency coordinates and build group affect build configuration. Keep formatting changes behavior-neutral and ensure documentation matches the resulting source layout.
- **Verification:** `./gradlew test`
- **Commit:** One commit for this step after tests pass.

## Progress checklist

- [x] Step 1 — Executor guards (already implemented; `./gradlew test` passed)
- [x] Step 2 — Move generator registry (`./gradlew test` passed)
- [x] Step 3 — Direction tables and shared generator base (`./gradlew test` passed)
- [x] Step 4 — Executed moves, capture positions, and atomic undo (`./gradlew test` passed; prior API/atomicity work reused)
- [ ] Step 5 — Spring wiring and package-cycle removal
- [ ] Step 6 — Model and board cleanup
- [ ] Step 7 — Remaining behavioral tests
- [ ] Step 8 — Build and repository hygiene
