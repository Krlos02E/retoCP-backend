package com.cinetest.controller;

import com.cinetest.dto.ApiResponseDTO;
import com.cinetest.dto.LoginRequest;
import com.cinetest.dto.RegisterRequest;
import com.cinetest.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Handles user registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and JWT-based login")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user.
     *
     * @param dto the registration request containing username, password and role
     * @return a structured response indicating successful registration
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with a username, password and role. Returns 201 on success, 400 on validation error, 409 if username already exists.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body or validation error", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Username already exists", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<Void>> register(@Valid @RequestBody RegisterRequest dto) {
        authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "User registered successfully"));
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param dto the login request containing username and password
     * @return a structured response containing the JWT token
     */
    @PostMapping("/login")
    @Operation(
            summary = "Login and obtain JWT token",
            description = "Authenticates a user with username and password. Returns a JWT token to be used in the Authorization header for protected endpoints.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful, token returned", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
            }
    )
    public ResponseEntity<ApiResponseDTO<String>> login(@Valid @RequestBody LoginRequest dto) {
        String token = authService.login(dto);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK.value(), "Login successful", token));
    }
}
