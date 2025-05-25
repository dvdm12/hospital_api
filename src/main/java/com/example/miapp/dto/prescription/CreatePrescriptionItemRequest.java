package com.example.miapp.dto.prescription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for creating prescription items
 */
@Data
public class CreatePrescriptionItemRequest {
    @NotBlank(message = "Medication name is required")
    @Size(max = 100, message = "Medication name must not exceed 100 characters")
    private String medicationName;
    
    @NotBlank(message = "Dosage is required")
    @Size(max = 100, message = "Dosage must not exceed 100 characters")
    private String dosage;
    
    @NotBlank(message = "Frequency is required")
    @Size(max = 100, message = "Frequency must not exceed 100 characters")
    private String frequency;
    
    @Size(max = 100, message = "Duration must not exceed 100 characters")
    private String duration;
    
    @Size(max = 500, message = "Instructions must not exceed 500 characters")
    private String instructions;
    
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    @Size(max = 50, message = "Route must not exceed 50 characters")
    private String route;
    
    private boolean refillable = false;
    
    private Integer refillsAllowed = 0;
}