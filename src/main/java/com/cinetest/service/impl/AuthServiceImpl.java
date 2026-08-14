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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link AuthService}.
 * Handles user registration and JWT-based authentication.
 */
@Service
@RequiredArgsConstructor
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
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessRuleException("User already exists");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();

        return userRepository.save(user);
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
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessRuleException("Invalid credentials");
        }

        return jwtUtil.generateToken(user);
    }
}
