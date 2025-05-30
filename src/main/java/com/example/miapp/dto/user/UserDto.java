package com.example.miapp.dto.user;

import com.example.miapp.entity.User.UserStatus;
import lombok.Data;

import java.util.Set;

/**
 * DTO for User responses
 */
@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String cc; 
    private UserStatus status;
    private Long lastLogin;
    private boolean firstLogin;
    private Set<String> roles;
}