package com.sameer.chess.api.dto;

/** Stable JSON error body returned by the REST API. */
public record ApiErrorDto(String code, String message) {
}
