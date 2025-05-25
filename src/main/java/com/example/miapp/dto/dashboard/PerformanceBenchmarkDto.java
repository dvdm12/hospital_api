package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for performance benchmark comparisons
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceBenchmarkDto {
    private double completionRateChange;
    private double revenueChange;
    private int patientGrowthChange;
    private double utilizationChange;
    private double noShowRateChange;
    private double cancellationRateChange;
    private double averageWaitTimeChange;
    private double patientSatisfactionChange;
    private String overallPerformance; // IMPROVED, DECLINED, STABLE
    private double performanceScore; // 0.0 to 100.0
    private String bestImprovement;
    private String biggestConcern;
    private String period; // DAILY, WEEKLY, MONTHLY, QUARTERLY
}