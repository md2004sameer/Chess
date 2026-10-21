package com.sameer.chess.model;

import java.util.Objects;

/** Reports whether a requested move is valid and explains rejected moves. */
public record ValidationResult(boolean valid, String message) {
    public ValidationResult {
        Objects.requireNonNull(message, "message");
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, "OK");
    }

    public static ValidationResult reject(String message) {
        return new ValidationResult(false, message);
    }
}
