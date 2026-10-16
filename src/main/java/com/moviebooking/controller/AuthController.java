
package com.moviebooking.controller;

import com.moviebooking.dto.ApiResponse;
import com.moviebooking.dto.GoogleAuthRequest;
import com.moviebooking.dto.LoginRequest;
import com.moviebooking.dto.RegisterRequest;
import com.moviebooking.dto.UserDto;
import com.moviebooking.model.User;
import com.moviebooking.service.UserService;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public static final String SESSION_USER_KEY = "LOGGED_IN_USER";

    @Autowired
    private UserService userService;

    @Value("${google.client.id:${GOOGLE_CLIENT_ID:}}")
    private String googleClientId;

    @GetMapping("/google/client-id")
    public ResponseEntity<ApiResponse<Map<String, String>>> getGoogleClientId() {
        return ResponseEntity.ok(ApiResponse.success(
                "Google Client ID retrieved",
                Map.of("clientId", googleClientId != null ? googleClientId : "")
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpSession session) {

        try {
            User user = userService.registerUser(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Registration successful! Please sign in with your email and password.",
                            UserDto.fromEntity(user)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to register. Please try again later."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {

        try {
            User user = userService.loginUser(request);

            session.setAttribute(SESSION_USER_KEY, user);

            return ResponseEntity.ok(ApiResponse.success(
                    "Login successful! Welcome back, " + user.getName(),
                    UserDto.fromEntity(user)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during login. Please try again."));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<UserDto>> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpSession session) {

        if (request == null || request.getCredential() == null || request.getCredential().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("A valid Google ID token credential is required."));
        }

        if (googleClientId == null || googleClientId.isBlank()) {
            logger.error("Google Client ID is not configured on the server.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Google Sign-In is not configured on the server."));
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId.trim()))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getCredential());

            if (idToken == null) {
                logger.warn("Google ID token verification failed: token signature or audience invalid.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired Google ID token."));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                logger.warn("Google authentication rejected: email address is not verified by Google.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Google email address is not verified."));
            }

            String email = payload.getEmail();
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Google account email is missing from token payload."));
            }

            String name = (String) payload.get("name");
            if (name == null || name.isBlank()) {
                name = email.split("@")[0];
            }

            User user = userService.processGoogleLogin(email, name);

            session.setAttribute(SESSION_USER_KEY, user);
            logger.info(">>> Google Sign-In successful for verified email [{}] ({})", email, name);

            return ResponseEntity.ok(ApiResponse.success(
                    "Google authentication successful!",
                    UserDto.fromEntity(user)));

        } catch (Exception e) {
            logger.error("Google authentication error during token verification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Google authentication failed: " + e.getMessage()));
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(HttpSession session) {

        User user = (User) session.getAttribute(SESSION_USER_KEY);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }

        return ResponseEntity.ok(ApiResponse.success(
                "User authenticated",
                UserDto.fromEntity(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully"));
    }
}
