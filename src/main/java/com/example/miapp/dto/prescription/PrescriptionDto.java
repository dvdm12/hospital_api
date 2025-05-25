package com.example.miapp.dto.prescription;

import com.example.miapp.entity.Prescription.PrescriptionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Prescription responses
 */
@Data
public class PrescriptionDto {
    private Long id;
    private String doctorName;
    private String patientName;
    private LocalDateTime issueDate;
    private String diagnosis;
    private String notes;
    private PrescriptionStatus status;
    private Long appointmentId;
    private boolean printed;
    private LocalDateTime printDate;
    private List<PrescriptionItemDto> medicationItems;
}