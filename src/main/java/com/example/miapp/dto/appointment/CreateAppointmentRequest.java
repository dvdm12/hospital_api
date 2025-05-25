package com.example.miapp.dto.appointment;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for creating new appointments
 */
@Data
public class CreateAppointmentRequest {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Appointment date must be in the present or future")
    private LocalDateTime date;
    
    private LocalDateTime endTime;
    
    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    @Size(max = 50, message = "Location must not exceed 50 characters")
    private String location;
}