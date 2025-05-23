package com.example.miapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

/**
 * DTO for transferring medical record data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MedicalRecordDto {

    /** Unique identifier for the medical record. */
    private Long id;

    /** Diagnosis information. Cannot be null. */
    @NotNull(message = "Diagnosis cannot be null")
    @Size(min = 5, max = 255, message = "Diagnosis must be between 5 and 255 characters")
    private String diagnosis;

    /** Treatment details. Cannot be null. */
    @NotNull(message = "Treatment cannot be null")
    @Size(min = 5, max = 255, message = "Treatment must be between 5 and 255 characters")
    private String treatment;

    /** Date of record entry. Must be in the past or present. */
    @PastOrPresent(message = "Entry date cannot be in the future")
    private Date entryDate;

    /** The doctor responsible for this medical record. */
    @NotNull(message = "Responsible doctor ID cannot be null")
    @Positive(message = "Responsible doctor ID must be positive")
    private Long responsibleDoctorId;

    /** The patient associated with this medical record. */
    @NotNull(message = "Patient ID cannot be null")
    @Positive(message = "Patient ID must be positive")
    private Long patientId;
}
