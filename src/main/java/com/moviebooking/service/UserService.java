package com.moviebooking.service;

import com.moviebooking.dto.LoginRequest;
import com.moviebooking.dto.RegisterRequest;
import com.moviebooking.model.User;
import com.moviebooking.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(10));
        User user = new User(
                request.getName().trim(),
                request.getEmail().trim().toLowerCase(),
                hashedPassword,
                request.getPhone().trim()
        );

        return userRepository.save(user);
    }

    public User loginUser(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        // Requirement 1: After a successful login, a login notification should be sent to user's registered email
        try {
            emailService.sendLoginNotification(user);
        } catch (Exception e) {
            // Non-blocking: log and continue
        }

        return user;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }
}
