package com.example.miapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user in the system.
 */
@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"password", "roles"})
@EqualsAndHashCode(exclude = {"password", "roles"})
public class User {

    /** Unique identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username for login. */
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    /** User's email address. */
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    /** Hashed password. */
    @NotBlank
    @Size(max = 120)
    @JsonIgnore
    private String password;

    /** Account status (active, inactive, blocked). */
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /** Last login timestamp. */
    private Long lastLogin;

    /** User's roles. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", 
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    /** Flag to indicate if first login has occurred. */
    @Builder.Default
    private boolean firstLogin = true;
    
    /**
     * Checks if the user has a specific role.
     * 
     * @param roleName Name of the role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().name().equals(roleName));
    }
    
    /**
     * Enum representing possible user statuses.
     */
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        BLOCKED
    }
}