package com.sameer.chess.api.dto;

/** Stable JSON representation of a requested, legal, or historical move. */
public record MoveDto(String from, String to, String promotion) {
}
