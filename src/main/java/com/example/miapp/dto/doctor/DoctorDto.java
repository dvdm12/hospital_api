package com.example.miapp.dto.doctor;

import lombok.Data;

import java.util.List;

/**
 * DTO for Doctor entity responses
 */
@Data
public class DoctorDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String email;
    private String licenseNumber;
    private String profilePicture;
    private String biography;
    private Double consultationFee;
    private List<String> specialties;
    private List<DoctorScheduleDto> workSchedules;
}