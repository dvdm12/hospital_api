package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for resource utilization analysis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUtilizationDto {
    private double averageDoctorUtilization;
    private String mostUtilizedDoctor;
    private String leastUtilizedDoctor;
    private double highestUtilization;
    private double lowestUtilization;
    private String mostDemandedSpecialty;
    private String leastDemandedSpecialty;
    private Map<String, Double> utilizationBySpecialty;
    private Map<String, Double> utilizationByDoctor;
    private Map<String, Integer> appointmentsByHour;
    private Map<String, Integer> appointmentsByDay;
    private int totalAvailableSlots;
    private int totalBookedSlots;
    private double overallCapacityUtilization;
    private String capacityStatus; // UNDERUTILIZED, OPTIMAL, OVERBOOKED
    private String recommendations;
}