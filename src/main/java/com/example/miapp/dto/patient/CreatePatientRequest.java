package com.example.miapp.dto.patient;

import com.example.miapp.entity.Patient.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;

/**
 * DTO for creating new patients
 */
@Data
public class CreatePatientRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    
    @Past(message = "Birth date must be in the past")
    private Date birthDate;
    
    @NotBlank(message = "Phone is required")
    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;
    
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    private Gender gender;
    
    @Size(max = 5, message = "Blood type must not exceed 5 characters")
    private String bloodType;
    
    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;
    
    @Size(max = 15, message = "Emergency contact phone must not exceed 15 characters")
    private String emergencyContactPhone;
    
    @Size(max = 100, message = "Insurance provider must not exceed 100 characters")
    private String insuranceProvider;
    
    @Size(max = 50, message = "Insurance policy number must not exceed 50 characters")
    private String insurancePolicyNumber;
    
    // User account fields
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 120, message = "Password must be between 6 and 120 characters")
    private String password;
}