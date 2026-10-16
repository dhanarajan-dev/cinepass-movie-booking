package com.moviebooking.service;

import com.moviebooking.dto.LoginRequest;
import com.moviebooking.dto.RegisterRequest;
import com.moviebooking.model.User;
import com.moviebooking.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public User registerUser(RegisterRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(10));
        User user = new User(
                request.getName().trim(),
                cleanEmail,
                hashedPassword,
                request.getPhone().trim()
        );

        return userRepository.save(user);
    }

    public User loginUser(LoginRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(cleanEmail);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        // Send login notification to user's registered email
        try {
            emailService.sendLoginNotification(user);
        } catch (Exception e) {
            logger.warn("Non-blocking failure sending login notification for user {}: {}", user.getId(), e.getMessage());
        }

        return user;
    }

    @Transactional
    public User processGoogleLogin(String email, String name) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be empty for Google login.");
        }

        final String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(cleanEmail);
        User user;

        if (userOpt.isPresent()) {
            // Safe Account-Linking Policy:
            // 1. Reuse the existing account tied to this verified email address.
            // 2. Preserve existing password hash, phone number, and all existing bookings intact.
            // 3. Update name only if currently blank or missing.
            user = userOpt.get();
            if (name != null && !name.trim().isEmpty() && (user.getName() == null || user.getName().isBlank())) {
                user.setName(name.trim());
                user = userRepository.save(user);
            }
            logger.info("Reused existing account for Google authentication: [ID: {}, Email: {}]", user.getId(), cleanEmail);
        } else {
            // Register a new user account with verified Google email
            String randomPass = UUID.randomUUID().toString();
            String hashedPassword = BCrypt.hashpw(randomPass, BCrypt.gensalt(10));
            String displayName = (name != null && !name.trim().isEmpty()) 
                    ? name.trim() 
                    : cleanEmail.split("@")[0];

            User newUser = new User(displayName, cleanEmail, hashedPassword, "Google Auth");

            try {
                user = userRepository.save(newUser);
                logger.info("Created new user account via Google authentication: [ID: {}, Email: {}]", user.getId(), cleanEmail);
            } catch (DataIntegrityViolationException dive) {
                // Concurrency Protection: If a simultaneous request created the record, reload it
                logger.info("Handled concurrent user creation for [{}], fetching existing record", cleanEmail);
                user = userRepository.findByEmailIgnoreCase(cleanEmail)
                        .orElseThrow(() -> dive);
            }
        }

        // Send login notification email to user's verified Google email
        try {
            emailService.sendLoginNotification(user);
        } catch (Exception e) {
            logger.warn("Non-blocking failure sending Google login notification for user {}: {}", user.getId(), e.getMessage());
        }

        return user;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT));
    }
}

