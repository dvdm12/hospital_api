package com.example.miapp.dto.patient;

import com.example.miapp.entity.Patient.Gender;
import lombok.Data;

import java.util.Date;

/**
 * DTO for Patient entity responses
 */
@Data
public class PatientDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private Date birthDate;
    private int age;
    private String phone;
    private String address;
    private Gender gender;
    private String bloodType;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String insuranceProvider;
    private String insurancePolicyNumber;
}