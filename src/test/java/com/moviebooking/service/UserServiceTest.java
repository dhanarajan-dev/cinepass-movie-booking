package com.moviebooking.service;

import com.moviebooking.dto.RegisterRequest;
import com.moviebooking.model.User;
import com.moviebooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User("Dhanarajan", "dhanaraj082001@gmail.com", "$2a$10$hashedPassword", "6374520824");
        existingUser.setId(1L);
    }

    @Test
    @DisplayName("processGoogleLogin should reuse existing account and preserve existing password")
    void processGoogleLogin_ReusesExistingUser() {
        when(userRepository.findByEmailIgnoreCase("dhanaraj082001@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        User result = userService.processGoogleLogin("Dhanaraj082001@GMAIL.com", "Dhanaraj Google");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("dhanaraj082001@gmail.com");
        assertThat(result.getPassword()).isEqualTo("$2a$10$hashedPassword"); // Password must not be modified!
        assertThat(result.getPhone()).isEqualTo("6374520824"); // Phone must not be overwritten!
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("processGoogleLogin should create new account when user does not exist")
    void processGoogleLogin_CreatesNewUser() {
        when(userRepository.findByEmailIgnoreCase("newuser@gmail.com"))
                .thenReturn(Optional.empty());

        User createdUser = new User("New User", "newuser@gmail.com", "$2a$10$randomHash", "Google Auth");
        createdUser.setId(2L);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        User result = userService.processGoogleLogin("newuser@gmail.com", "New User");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getEmail()).isEqualTo("newuser@gmail.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendLoginNotification(result);
    }

    @Test
    @DisplayName("processGoogleLogin handles concurrent creation safely")
    void processGoogleLogin_HandlesConcurrentRaceCondition() {
        when(userRepository.findByEmailIgnoreCase("race@gmail.com"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        User result = userService.processGoogleLogin("race@gmail.com", "Race User");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("registerUser rejects duplicate email with different case")
    void registerUser_RejectsDuplicateCaseInsensitiveEmail() {
        when(userRepository.existsByEmailIgnoreCase("dhanaraj082001@gmail.com"))
                .thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setName("Test");
        request.setEmail("DHANARAJ082001@GMAIL.COM");
        request.setPhone("1234567890");
        request.setPassword("Secret123");

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("An account with this email address already exists");
    }
}
