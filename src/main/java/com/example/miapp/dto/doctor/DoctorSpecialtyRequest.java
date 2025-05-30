package com.example.miapp.dto.doctor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

/**
 * DTO for doctor specialty relationships
 */
@Data
public class DoctorSpecialtyRequest {
    
    @NotNull(message = "Specialty ID is required")
    private Long specialtyId;
    
    @Size(min = 3, max = 50, message = "Experience level must be between 3 and 50 characters")
    private String experienceLevel = "Junior"; // Default value
    
    @PastOrPresent(message = "Certification date must be in the past or present")
    private Date certificationDate = new Date(); // Default value (current date)
    
    /**
     * Default constructor
     */
    public DoctorSpecialtyRequest() {
    }
    
    /**
     * Constructor with specialty ID
     */
    public DoctorSpecialtyRequest(Long specialtyId) {
        this.specialtyId = specialtyId;
    }
    
    /**
     * Full constructor
     */
    public DoctorSpecialtyRequest(Long specialtyId, String experienceLevel, Date certificationDate) {
        this.specialtyId = specialtyId;
        this.experienceLevel = experienceLevel;
        this.certificationDate = certificationDate;
    }
}