package com.example.miapp.dto.doctor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    
    @NotBlank(message = "Identification number (CC) is required")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "Identification number must be between 8 and 12 digits")
    @Size(max = 20, message = "Identification number must not exceed 20 characters")
    private String cc;
    
    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number must not exceed 50 characters")
    private String licenseNumber;
    
    @Size(max = 1000, message = "Biography must not exceed 1000 characters")
    private String biography;
    
    @DecimalMin(value = "0.0", message = "Consultation fee must be positive or zero")
    @DecimalMax(value = "10000.0", message = "Consultation fee must not exceed 10000")
    private Double consultationFee = 0.0; // Default value
    
    private String profilePicture;
    
    @NotEmpty(message = "Al menos una especialidad debe ser asignada")
    @Valid
    private Set<DoctorSpecialtyRequest> specialties = new HashSet<>();
    
    // User account fields
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 120, message = "Password must be between 6 and 120 characters")
    private String password;
    
    /**
     * Default constructor initializes collections
     */
    public CreateDoctorRequest() {
        this.specialties = new HashSet<>();
    }
    
    /**
     * Helper method to check if any specialties are selected
     */
    public boolean hasSpecialties() {
        return specialties != null && !specialties.isEmpty();
    }
    
    /**
     * Helper method to extract just the specialty IDs
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Set<Long> getSpecialtyIds() {
        if (specialties == null) {
            return new HashSet<>();
        }
        return specialties.stream()
                .map(DoctorSpecialtyRequest::getSpecialtyId)
                .collect(Collectors.toSet());
    }
    
    /**
     * Returns consultation fee with default value if null
     */
    public Double getConsultationFee() {
        return this.consultationFee != null ? this.consultationFee : 0.0;
    }
    
    /**
     * Helper method to add a specialty
     */
    public void addSpecialty(DoctorSpecialtyRequest specialty) {
        if (specialty != null) {
            if (specialties == null) {
                specialties = new HashSet<>();
            }
            specialties.add(specialty);
        }
    }
}