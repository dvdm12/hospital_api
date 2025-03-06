package com.example.miapp.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for transferring appointment data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AppointmentDto {

    private Long id;

    @Future(message = "Appointment date must be in the future")
    private LocalDateTime date;

    @NotNull(message = "Patient ID cannot be null")
    private Long patientId;

    @NotNull(message = "Doctor ID cannot be null")
    private Long doctorId;

    @NotNull(message = "Reason cannot be null")
    private String reason;

    private String status;
}
