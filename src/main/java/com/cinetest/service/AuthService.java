package com.cinetest.service;

import com.cinetest.dto.LoginRequest;
import com.cinetest.dto.RegisterRequest;
import com.cinetest.model.entity.User;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Registers a new user.
     *
     * @param dto the registration request
     * @return the persisted user entity
     */
    User register(RegisterRequest dto);

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param dto the login request
     * @return the JWT token string
     */
    String login(LoginRequest dto);
}
