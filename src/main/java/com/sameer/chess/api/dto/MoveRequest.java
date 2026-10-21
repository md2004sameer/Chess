package com.sameer.chess.api.dto;

/** JSON input for a move, using algebraic square names and an optional q/r/b/n promotion. */
public record MoveRequest(String from, String to, String promotion) {
}
