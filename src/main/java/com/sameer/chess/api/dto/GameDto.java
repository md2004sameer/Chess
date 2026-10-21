package com.sameer.chess.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stable game snapshot with rank 8 first and file a first in each board row. */
public record GameDto(
        String gameId,
        String status,
        String activeColor,
        List<List<PieceDto>> board,
        int moveCount,
        boolean canClaimDraw,
        List<String> sanHistory) {

    public GameDto(String gameId, String status, String activeColor,
                   List<List<PieceDto>> board, int moveCount) {
        this(gameId, status, activeColor, board, moveCount, false, List.of());
    }

    public GameDto(String gameId, String status, String activeColor,
                   List<List<PieceDto>> board, int moveCount, boolean canClaimDraw) {
        this(gameId, status, activeColor, board, moveCount, canClaimDraw, List.of());
    }

    public GameDto {
        board = board.stream()
                .map(row -> Collections.unmodifiableList(new ArrayList<>(row)))
                .toList();
        sanHistory = List.copyOf(sanHistory);
    }
}
