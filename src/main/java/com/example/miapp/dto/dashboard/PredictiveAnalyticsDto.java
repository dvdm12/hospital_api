package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for predictive analytics data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictiveAnalyticsDto {
    private int predictedAppointments;
    private Double predictedRevenue;
    private int predictedNewPatients;
    private double predictedNoShowRate;
    private double predictedCancellationRate;
    private double confidenceLevel;
    private String predictionPeriod; // NEXT_WEEK, NEXT_MONTH, NEXT_QUARTER
    private LocalDate predictionDate;
    private Map<String, Integer> predictedAppointmentsBySpecialty;
    private Map<String, Double> predictedRevenueBySpecialty;
    private String highestDemandSpecialty;
    private String lowestDemandSpecialty;
    private double capacityWarning; // 0.0 to 1.0 (1.0 = serious capacity issue predicted)
    private String riskFactors;
    private String opportunities;
    private String modelAccuracy;
}