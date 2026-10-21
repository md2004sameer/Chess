package com.sameer.chess.api.error;

import org.springframework.http.HttpStatus;

/** A well-formed move request rejected by the chess rules engine. */
public final class IllegalMoveException extends ApiException {
    public IllegalMoveException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, "ILLEGAL_MOVE", message);
    }
}
