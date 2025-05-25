package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for specialty statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyStatsDto {
    private Long specialtyId;
    private String specialtyName;
    private long appointmentCount;
    private long doctorCount;
    private double averageFee;
}