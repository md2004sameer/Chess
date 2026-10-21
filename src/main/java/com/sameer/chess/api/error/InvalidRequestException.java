package com.sameer.chess.api.error;

import org.springframework.http.HttpStatus;

/** A request contains malformed or unsupported input. */
public final class InvalidRequestException extends ApiException {
    public InvalidRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
    }
}
