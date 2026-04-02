package com.app.authsystem.controller;

import com.app.authsystem.config.JwtUtil;
import com.app.authsystem.dto.LoginRequest;
import com.app.authsystem.dto.RegisterRequest;
import com.app.authsystem.service.AuthService;
import com.app.authsystem.entity.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.loginAndGetUser(request);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("role", user.getRole());
        response.put("token", token);
        return response;
    }
}