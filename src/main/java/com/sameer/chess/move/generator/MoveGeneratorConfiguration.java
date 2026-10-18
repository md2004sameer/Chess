package com.sameer.chess.move.generator;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MoveGeneratorConfiguration {

    @Bean
    public MoveGeneratorRegistry moveGeneratorRegistry() {
        return new MoveGeneratorRegistry(List.of(
                new KingMoveGenerator(),
                new QueenMoveGenerator(),
                new RookMoveGenerator(),
                new BishopMoveGenerator(),
                new KnightMoveGenerator(),
                new PawnMoveGenerator()));
    }
}
