package com.example.miapp.dto;

import jakarta.validation.constraints.NotNull;
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

    private Long id;

    @NotNull(message = "Diagnosis cannot be null")
    private String diagnosis;

    @NotNull(message = "Treatment cannot be null")
    private String treatment;

    private Date entryDate;

    @NotNull(message = "Responsible doctor cannot be null")
    private String responsibleDoctor;

    @NotNull(message = "Patient ID cannot be null")
    private Long patientId;
}
