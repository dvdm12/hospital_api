package com.example.miapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for transferring appointment data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AppointmentDto {

    /** Unique identifier for the appointment. */
    @Positive(message = "ID must be positive")
    private Long id;

    /** Date and time of the appointment. Must be in the present or future. */
    @FutureOrPresent(message = "Appointment date must be in the present or future")
    private LocalDateTime date;

    /** The patient attending the appointment. Cannot be null. */
    @NotNull(message = "Patient ID cannot be null")
    private Long patientId;

    /** The doctor assigned to the appointment. Cannot be null. */
    @NotNull(message = "Doctor ID cannot be null")
    private Long doctorId;

    /** Reason for the appointment. Cannot be null. */
    @NotNull(message = "Reason cannot be null")
    @Size(min = 5, max = 255, message = "Reason must be between 5 and 255 characters")
    private String reason;

    /** Status of the appointment (e.g., Scheduled, Completed, Canceled). */
    @Pattern(regexp = "^(Scheduled|Completed|Canceled)$", message = "Status must be 'Scheduled', 'Completed', or 'Canceled'")
    private String status;
}
