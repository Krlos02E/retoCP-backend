package com.cinetest.dto;

import com.cinetest.model.enums.Role;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String username;
    private Role role;
}
