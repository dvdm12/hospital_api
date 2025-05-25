package com.example.miapp.dto.doctor;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

/**
 * DTO for creating new doctors
 */
@Data
public class CreateDoctorRequest {
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    
    @NotBlank(message = "Phone is required")
    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @NotBlank(message = "License number is required")
    @Size(max = 20, message = "License number must not exceed 20 characters")
    private String licenseNumber;
    
    @Size(max = 1000, message = "Biography must not exceed 1000 characters")
    private String biography;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fee must be positive")
    @DecimalMax(value = "10000.0", message = "Consultation fee must not exceed 10000")
    private Double consultationFee;
    
    private String profilePicture;
    
    @NotEmpty(message = "At least one specialty is required")
    private Set<Long> specialtyIds;
    
    // User account fields
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 120, message = "Password must be between 6 and 120 characters")
    private String password;
}