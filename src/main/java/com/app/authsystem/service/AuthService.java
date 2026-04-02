package com.app.authsystem.service;

import com.app.authsystem.dto.LoginRequest;
import com.app.authsystem.dto.RegisterRequest;
import com.app.authsystem.entity.User;
import com.app.authsystem.exception.DuplicateResourceException;
import com.app.authsystem.exception.ResourceNotFoundException;
import com.app.authsystem.exception.UnauthorizedException;
import com.app.authsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password
        user.setRole(request.getRole() != null ? request.getRole().toUpperCase() : "USER");

        userRepository.save(user);

        return "User registered successfully!";
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return "Login successful!";
    }

    public User loginAndGetUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return user;
    }
}