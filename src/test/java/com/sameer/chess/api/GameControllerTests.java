package com.sameer.chess.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest(properties = "app.cors.allowed-origins=https://frontend.example")
@AutoConfigureMockMvc
class GameControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsAndReadsGameSnapshotsWithExplicitBoardShape() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/games"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.gameId").isString())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.activeColor").value("WHITE"))
                .andExpect(jsonPath("$.board", hasSize(8)))
                .andExpect(jsonPath("$.board[0]", hasSize(8)))
                .andExpect(jsonPath("$.board[0][0].color").value("BLACK"))
                .andExpect(jsonPath("$.board[7][4].type").value("KING"))
                .andReturn();
        String gameId = UUID.fromString(
                URI.create(created.getResponse().getHeader("Location")).getPath()
                        .substring("/api/games/".length())).toString();

        mockMvc.perform(get("/api/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.moveCount").value(0))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertFalse(
                        result.getResponse().getContentAsString().contains("com.sameer.chess")));
    }

    @Test
    void exposesLegalMovesAndMoveHistoryAsMoveDtos() throws Exception {
        String gameId = createGameId();

        mockMvc.perform(get("/api/games/{gameId}/legal-moves", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)))
                .andExpect(jsonPath("$[0].from").isString())
                .andExpect(jsonPath("$[0].to").isString());

        mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"e2\",\"to\":\"e4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeColor").value("BLACK"))
                .andExpect(jsonPath("$.moveCount").value(1))
                .andExpect(jsonPath("$.sanHistory[0]").value("e4"));

        mockMvc.perform(get("/api/games/{gameId}/moves", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].from").value("e2"))
                .andExpect(jsonPath("$[0].to").value("e4"))
                .andExpect(jsonPath("$[0].promotion").isEmpty());
    }

    @Test
    void distinguishesMalformedRequestsIllegalMovesAndUnknownGames() throws Exception {
        String gameId = createGameId();
        mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"e9\",\"to\":\"e4\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"e2\",\"to\":\"e4\",\"promotion\":\"king\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"e7\",\"to\":\"e5\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("ILLEGAL_MOVE"));

        mockMvc.perform(get("/api/games/{gameId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GAME_NOT_FOUND"));
        mockMvc.perform(get("/api/games/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(get("/api/games/{gameId}", UUID.randomUUID())
                        .header("Origin", "https://unlisted.example"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void requiresEnginePromotionAndAcceptsExplicitPromotionOverHttp() throws Exception {
        String gameId = createGameId();
        play(gameId, "a2", "a4");
        play(gameId, "h7", "h5");
        play(gameId, "a4", "a5");
        play(gameId, "h5", "h4");
        play(gameId, "a5", "a6");
        play(gameId, "h4", "h3");
        play(gameId, "a6", "b7");
        play(gameId, "b8", "c6");

        mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"b7\",\"to\":\"b8\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value(
                        "A pawn reaching the last rank must be promoted."));
        mockMvc.perform(get("/api/games/{gameId}", gameId))
                .andExpect(jsonPath("$.moveCount").value(8));

        mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"b7\",\"to\":\"b8\",\"promotion\":\"q\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board[0][1].symbol").value("Q"));
        mockMvc.perform(get("/api/games/{gameId}/moves", gameId))
                .andExpect(jsonPath("$[8].promotion").value("q"));
    }

    @Test
    void reportsFinishedGameConflictAndEnforcesConfiguredCorsOrigins() throws Exception {
        String gameId = createGameId();
        play(gameId, "f2", "f3");
        play(gameId, "e7", "e5");
        play(gameId, "g2", "g4");
        play(gameId, "d8", "h4");

        mockMvc.perform(get("/api/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKMATE"));
        mockMvc.perform(get("/api/games/{gameId}/legal-moves", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"a2\",\"to\":\"a3\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("GAME_FINISHED"));

        mockMvc.perform(options("/api/games")
                        .header("Origin", "https://frontend.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin", "https://frontend.example"));
        mockMvc.perform(options("/api/games")
                        .header("Origin", "https://unlisted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void exposesDrawEligibilityAndSupportsResignationOverHttp() throws Exception {
        String gameId = createGameId();

        mockMvc.perform(get("/api/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canClaimDraw").value(false));

        mockMvc.perform(post("/api/games/{gameId}/draw", gameId))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("ILLEGAL_MOVE"));

        mockMvc.perform(post("/api/games/{gameId}/resign", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESIGNED"))
                .andExpect(jsonPath("$.activeColor").value("WHITE"))
                .andExpect(jsonPath("$.canClaimDraw").value(false));

        mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"e2\",\"to\":\"e4\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void claimsThreefoldDrawThroughTheApiWhenEligible() throws Exception {
        String gameId = createGameId();
        for (int repetition = 0; repetition < 2; repetition++) {
            play(gameId, "b1", "c3");
            play(gameId, "b8", "c6");
            play(gameId, "c3", "b1");
            play(gameId, "c6", "b8");
        }

        mockMvc.perform(get("/api/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canClaimDraw").value(true));
        mockMvc.perform(post("/api/games/{gameId}/draw", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAW_THREEFOLD_REPETITION"))
                .andExpect(jsonPath("$.canClaimDraw").value(false));
    }

    private String createGameId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/games"))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(
                URI.create(result.getResponse().getHeader("Location")).getPath()
                        .substring("/api/games/".length())).toString();
    }

    private void play(String gameId, String from, String to) throws Exception {
        mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"" + from + "\",\"to\":\"" + to + "\"}"))
                .andExpect(status().isOk());
    }
}
