package com.example.miapp.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
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

    private Long id;

    @NotNull(message = "Patient ID cannot be null")
    private Long patientId;

    @NotNull(message = "Room ID cannot be null")
    private Long roomId;

    @FutureOrPresent(message = "Check-in date must be today or in the future")
    private Date checkInDate;

    private Date checkOutDate;

    private String observations;
}
