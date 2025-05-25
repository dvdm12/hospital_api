package com.example.miapp.dto.medicalrecord;

import com.example.miapp.entity.MedicalRecordEntry.EntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for creating new medical record entries
 */
@Data
public class CreateMedicalEntryRequest {
    @NotNull(message = "Entry type is required")
    private EntryType type;
    
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content must not exceed 2000 characters")
    private String content;
    
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    private Long appointmentId;
    
    private boolean visibleToPatient = true;
    
    @Size(max = 1000, message = "Attachments must not exceed 1000 characters")
    private String attachments;
}