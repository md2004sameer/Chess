package com.sameer.chess.api.dto;

/** Stable JSON representation of one occupied board square. */
public record PieceDto(String color, String type, String symbol) {
}
