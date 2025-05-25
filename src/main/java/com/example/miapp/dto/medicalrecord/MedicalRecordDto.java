package com.example.miapp.dto.medicalrecord;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Medical Record responses
 */
@Data
public class MedicalRecordDto {
    private Long id;
    private Long patientId;
    private String patientName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String allergies;
    private String familyHistory;
    private String chronicConditions;
    private String currentMedications;
    private String surgicalHistory;
    private String notes;
    private List<MedicalRecordEntryDto> entries;
}