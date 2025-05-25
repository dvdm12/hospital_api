package com.example.miapp.dto.patient;

import com.example.miapp.entity.Patient.Gender;
import lombok.Data;

/**
 * DTO for patient search criteria
 */
@Data
public class PatientSearchCriteria {
    private String name;
    private String email;
    private String phone;
    private Gender gender;
    private Integer minAge;
    private Integer maxAge;
    private String insuranceProvider;
    private String condition;
    private String allergy;
    private String medicationName;
}