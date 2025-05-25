package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for operational efficiency metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationalEfficiencyDto {
    private double appointmentEfficiency;
    private double revenuePerDoctor;
    private double patientsPerDoctor;
    private double overallEfficiency;
    private double averageAppointmentDuration;
    private double patientWaitTime;
    private double doctorIdleTime;
    private double resourceUtilizationRate;
    private double operationalCostRatio;
    private double patientThroughput;
    private String efficiencyGrade; // A, B, C, D, F
    private String bottleneckArea;
    private String mostEfficientArea;
    private String improvementOpportunity;
    private double benchmarkComparison; // vs industry standard
}