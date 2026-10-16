package com.moviebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.dto.GoogleAuthRequest;
import com.moviebooking.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerGoogleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/auth/google/client-id returns configured client ID without exposing client secret")
    void getGoogleClientId_ReturnsClientIdOnly() throws Exception {
        mockMvc.perform(get("/api/auth/google/client-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clientId").exists())
                .andExpect(jsonPath("$.data.clientSecret").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/google rejects request with missing credential token")
    void googleAuth_RejectsMissingCredential() throws Exception {
        GoogleAuthRequest request = new GoogleAuthRequest();

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/google rejects unauthenticated raw email and name payload")
    void googleAuth_RejectsRawEmailPayloadWithoutToken() throws Exception {
        String rawBypassJson = "{\"email\": \"attacker@gmail.com\", \"name\": \"Attacker\"}";

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawBypassJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/google rejects invalid/tampered ID token")
    void googleAuth_RejectsInvalidToken() throws Exception {
        GoogleAuthRequest request = new GoogleAuthRequest("invalid.jwt.token");

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
