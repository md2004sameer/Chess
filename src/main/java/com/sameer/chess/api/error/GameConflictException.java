package com.sameer.chess.api.error;

import org.springframework.http.HttpStatus;

/** A request conflicts with the current lifecycle state of a game. */
public final class GameConflictException extends ApiException {
    public GameConflictException(String message) {
        super(HttpStatus.CONFLICT, "GAME_FINISHED", message);
    }
}
