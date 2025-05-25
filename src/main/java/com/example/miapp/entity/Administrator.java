package com.example.miapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an administrator in the hospital system.
 */
@Entity
@Table(name = "administrator")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
public class Administrator {

    /** Unique identifier for the administrator. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Administrator's first name. */
    @Column(nullable = false, length = 50)
    private String firstName;
    
    /** Administrator's last name. */
    @Column(nullable = false, length = 50)
    private String lastName;
    
    /** Administrator's phone number. */
    @Column(nullable = false, length = 15)
    private String phone;
    
    /** Administrator's email address. */
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    /** Administrator's position or title. */
    @Column(nullable = false, length = 50)
    private String position;
    
    /** Department the administrator belongs to. */
    @Column(length = 50)
    private String department;
    
    /** User account associated with this administrator. */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;
    
    /** Administrator's profile picture URL. */
    private String profilePicture;
    
    /** Date and time when the administrator was registered. */
    @Column(nullable = false)
    private LocalDateTime registeredAt;
    
    /** Admin permissions level. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionLevel permissionLevel;
    
    /**
     * Gets the full name of the administrator.
     * 
     * @return The full name (first name + last name)
     */
    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Enum representing administrator permission levels.
     */
    public enum PermissionLevel {
        SUPER_ADMIN,
        SYSTEM_ADMIN,
        DEPARTMENT_ADMIN,
        REPORT_ADMIN,
        USER_ADMIN
    }
    
    /**
     * Pre-persist hook to set registration timestamp.
     */
    @PrePersist
    public void prePersist() {
        this.registeredAt = LocalDateTime.now();
    }
}