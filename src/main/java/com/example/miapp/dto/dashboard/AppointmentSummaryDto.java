package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for appointment summary information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSummaryDto {
    private Long id;
    private LocalDateTime dateTime;
    private String patientName;
    private String doctorName;
    private String specialty;
    private String reason;
    private String status;
    private String location;
    private int durationMinutes;
    private boolean confirmed;
    private String priority; // HIGH, MEDIUM, LOW
    private String notes;
}