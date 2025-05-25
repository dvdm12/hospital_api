package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for trend analysis data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendAnalysisDto {
    private double appointmentTrend;
    private double revenueTrend;
    private double patientGrowthTrend;
    private double completionRateTrend;
    private double noShowRateTrend;
    private double cancellationRateTrend;
    private String trendDirection; // INCREASING, DECREASING, STABLE
    private double trendStrength; // 0.0 to 1.0
    private int dataPoints;
    private double confidenceLevel;
    private List<TrendDataPointDto> historicalData;
    private String interpretation;
    private List<String> recommendations;
}