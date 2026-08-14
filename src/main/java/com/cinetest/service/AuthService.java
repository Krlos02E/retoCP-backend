package com.cinetest.service;

import com.cinetest.dto.LoginRequest;
import com.cinetest.dto.RegisterRequest;
import com.cinetest.model.entity.User;

public interface AuthService {

    User register(RegisterRequest dto);

    String login(LoginRequest dto);
}
