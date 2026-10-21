package com.sameer.chess.api.error;

import java.util.UUID;
import org.springframework.http.HttpStatus;

/** The requested game UUID is not present in the in-memory registry. */
public final class GameNotFoundException extends ApiException {
    public GameNotFoundException(UUID gameId) {
        super(HttpStatus.NOT_FOUND, "GAME_NOT_FOUND", "No game exists with id " + gameId + ".");
    }
}
