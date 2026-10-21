package com.sameer.chess.game;

import com.sameer.chess.model.GameStatus;

/** Reports the result and resulting status of a requested game move. */
public record MoveResult(boolean accepted, String message, GameStatus status) {
}
