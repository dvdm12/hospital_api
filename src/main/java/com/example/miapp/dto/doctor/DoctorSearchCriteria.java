package com.example.miapp.dto.doctor;

import lombok.Data;

import java.time.DayOfWeek;

/**
 * DTO for doctor search criteria
 */
@Data
public class DoctorSearchCriteria {
    private String name;
    private Long specialtyId;
    private DayOfWeek dayOfWeek;
    private Double minFee;
    private Double maxFee;
    private String experienceLevel;
    private Boolean available;
}