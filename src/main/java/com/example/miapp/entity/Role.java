package com.example.miapp.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a role in the system.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Role {
    
    /** Unique identifier for the role. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    /** Name of the role. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;
    
    /**
     * Enum representing available roles in the system.
     */
    public enum ERole {
        ROLE_ADMIN,
        ROLE_DOCTOR,
        ROLE_PATIENT
    }
}