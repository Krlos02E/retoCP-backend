package com.cinetest.service.impl;

import com.cinetest.dto.LoginRequest;
import com.cinetest.dto.RegisterRequest;
import com.cinetest.exception.BusinessRuleException;
import com.cinetest.exception.ResourceNotFoundException;
import com.cinetest.model.entity.User;
import com.cinetest.repository.UserRepository;
import com.cinetest.service.AuthService;
import com.cinetest.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link AuthService}.
 * Handles user registration and JWT-based authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * {@inheritDoc}
     * Hashes the password before saving.
     *
     * @throws BusinessRuleException if the username already exists
     */
    @Override
    public User register(RegisterRequest dto) {
        log.info("Registering new user '{}' with role={}", dto.getUsername(), dto.getRole());
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            log.warn("Registration rejected: username '{}' already exists", dto.getUsername());
            throw new BusinessRuleException("User already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();

        User saved = userRepository.save(user);
        log.info("User registered with id={} username='{}'", saved.getId(), saved.getUsername());
        return saved;
    }

    /**
     * {@inheritDoc}
     * Validates credentials and generates a signed JWT token.
     *
     * @throws ResourceNotFoundException if the user does not exist
     * @throws BusinessRuleException     if the password is invalid
     */
    @Override
    public String login(LoginRequest dto) {
        log.debug("Login attempt for user '{}'", dto.getUsername());
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed for user '{}': invalid credentials", dto.getUsername());
            throw new BusinessRuleException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);
        log.info("User '{}' logged in successfully", dto.getUsername());
        return token;
    }
}
