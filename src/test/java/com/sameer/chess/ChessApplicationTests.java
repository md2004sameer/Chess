package com.sameer.chess;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sameer.chess.validation.MoveValidator;

@SpringBootTest
class ChessApplicationTests {

    @Autowired
    private MoveValidator moveValidator;

	@Test
	void contextLoads() {
        assertNotNull(moveValidator);
	}

}
