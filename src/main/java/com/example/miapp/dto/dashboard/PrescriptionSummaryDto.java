package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for prescription summary information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionSummaryDto {
    private Long id;
    private LocalDateTime issueDate;
    private String diagnosis;
    private String doctorName;
    private int medicationCount;
    private String status;
    private String primaryMedication;
    private boolean hasRefills;
    private int remainingRefills;
    private LocalDateTime expirationDate;
    private String urgency; // URGENT, ROUTINE
    private boolean printed;
}