package com.example.miapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

/**
 * DTO for transferring patient-room relation data.
 */
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
public class PatientRoomDto {

    /** Unique identifier for the patient-room relation. */
    @Positive(message = "ID must be positive")
    private Long id;

    /** The ID of the patient assigned to the room. Cannot be null. */
    @NotNull(message = "Patient ID cannot be null")
    private Long patientId;

    /** The ID of the room assigned to the patient. Cannot be null. */
    @NotNull(message = "Room ID cannot be null")
    private Long roomId;

    /** Check-in date of the patient in the room. Must be today or in the future. */
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    private Date checkInDate;

    /** Check-out date of the patient from the room. Must be today or in the future. */
    @FutureOrPresent(message = "Check-out date must be today or in the future")
    private Date checkOutDate;

    /** Additional observations about the patient stay. */
    @Size(max = 255, message = "Observations must not exceed 255 characters")
    private String observations;
}
