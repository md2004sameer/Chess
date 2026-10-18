package com.sameer.chess;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sameer.chess.move.MoveExecutor;
import com.sameer.chess.move.generator.MoveGeneratorRegistry;
import com.sameer.chess.move.rule.CastlingRule;
import com.sameer.chess.move.rule.EnPassantRule;
import com.sameer.chess.move.rule.PromotionRule;
import com.sameer.chess.move.rule.SpecialMoveRule;
import com.sameer.chess.validation.AttackDetector;
import com.sameer.chess.validation.MoveValidator;

@Configuration
public class ChessConfiguration {

    @Bean
    public MoveExecutor moveExecutor() {
        return new MoveExecutor();
    }

    @Bean
    public AttackDetector attackDetector() {
        return new AttackDetector();
    }

    @Bean
    public CastlingRule castlingRule() {
        return new CastlingRule();
    }

    @Bean
    public EnPassantRule enPassantRule() {
        return new EnPassantRule();
    }

    @Bean
    public PromotionRule promotionRule() {
        return new PromotionRule();
    }

    @Bean
    public MoveValidator moveValidator(
            MoveGeneratorRegistry generators,
            CastlingRule castlingRule,
            EnPassantRule enPassantRule,
            PromotionRule promotionRule,
            AttackDetector attackDetector,
            MoveExecutor moveExecutor) {
        return new MoveValidator(
                generators,
                specialMoveRules(castlingRule, enPassantRule, promotionRule),
                attackDetector,
                moveExecutor);
    }

    public static List<SpecialMoveRule> specialMoveRules(
            CastlingRule castlingRule,
            EnPassantRule enPassantRule,
            PromotionRule promotionRule) {
        return List.of(castlingRule, enPassantRule, promotionRule);
    }
}
