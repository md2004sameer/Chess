package com.sameer.chess.move.generator;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sameer.chess.model.enums.Type;

@Configuration
public class MoveGeneratorConfiguration {

    @Bean
    public MoveGeneratorRegistry moveGeneratorRegistry() {
        return new MoveGeneratorRegistry(Map.of(
                Type.KING, new KingMoveGenerator(),
                Type.QUEEN, new QueenMoveGenerator(),
                Type.ROOK, new RookMoveGenerator(),
                Type.BISHOP, new BishopMoveGenerator(),
                Type.KNIGHT, new KnightMoveGenerator(),
                Type.PAWN, new PawnMoveGenerator()));
    }
}
