package com.example.miapp.dto.medicalrecord;

import com.example.miapp.entity.MedicalRecordEntry.EntryType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for Medical Record Entry
 */
@Data
public class MedicalRecordEntryDto {
    private Long id;
    private LocalDateTime entryDate;
    private EntryType type;
    private String title;
    private String content;
    private String doctorName;
    private Long appointmentId;
    private boolean visibleToPatient;
    private String attachments;
}