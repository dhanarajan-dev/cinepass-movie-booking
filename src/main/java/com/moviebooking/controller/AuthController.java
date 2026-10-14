package com.moviebooking.controller;

import com.moviebooking.dto.ApiResponse;
import com.moviebooking.dto.LoginRequest;
import com.moviebooking.dto.RegisterRequest;
import com.moviebooking.dto.UserDto;
import com.moviebooking.model.User;
import com.moviebooking.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_USER_KEY = "LOGGED_IN_USER";

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        try {
            User user = userService.registerUser(request);
            // Requirement 1: User is NOT automatically logged in; must proceed to Login page
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful! Please sign in with your email and password.", UserDto.fromEntity(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to register. Please try again later."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        try {
            User user = userService.loginUser(request);
            session.setAttribute(SESSION_USER_KEY, user);
            return ResponseEntity.ok(ApiResponse.success("Login successful! Welcome back, " + user.getName(), UserDto.fromEntity(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred during login. Please try again."));
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success("User authenticated", UserDto.fromEntity(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.removeAttribute(SESSION_USER_KEY);
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
