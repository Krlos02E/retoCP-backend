package com.cinetest.service.impl;

import com.cinetest.dto.LoginRequest;
import com.cinetest.dto.RegisterRequest;
import com.cinetest.exception.BusinessRuleException;
import com.cinetest.exception.ResourceNotFoundException;
import com.cinetest.model.entity.User;
import com.cinetest.model.enums.Role;
import com.cinetest.repository.UserRepository;
import com.cinetest.service.AuthService;
import com.cinetest.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
