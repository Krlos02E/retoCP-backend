package com.cinetest.controller;

import com.cinetest.dto.ApiResponse;
import com.cinetest.dto.LoginRequest;
import com.cinetest.dto.RegisterRequest;
import com.cinetest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest dto) {
        authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest dto) {
        String token = authService.login(dto);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Login successful", token));
    }
}
