package com.app.authsystem.controller;

import com.app.authsystem.entity.User;
import com.app.authsystem.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public User getUser(@PathVariable @Positive(message = "id must be greater than 0") Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Page<User> userPage = userService.getUsersPaged(page, size, sortBy, direction);
        return ResponseEntity.ok(userPage);
    }
}