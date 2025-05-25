package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for medical entry summary information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalEntrySummaryDto {
    private Long id;
    private LocalDateTime entryDate;
    private String type;
    private String title;
    private String doctorName;
    private String specialty;
    private boolean visibleToPatient;
    private boolean hasAttachments;
    private String priority; // CRITICAL, HIGH, MEDIUM, LOW
    private String summary; // Brief content summary
}