package com.example.miapp.dto.prescription;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * DTO for creating new prescriptions
 */
@Data
public class CreatePrescriptionRequest {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotBlank(message = "Diagnosis is required")
    @Size(max = 500, message = "Diagnosis must not exceed 500 characters")
    private String diagnosis;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    private Long appointmentId;
    
    @NotEmpty(message = "At least one medication item is required")
    @Valid
    private List<CreatePrescriptionItemRequest> medicationItems;
}