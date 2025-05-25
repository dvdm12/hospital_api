package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for individual trend data points
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataPointDto {
    private LocalDate date;
    private double value;
    private String metric; // APPOINTMENTS, REVENUE, PATIENTS, etc.
    private double movingAverage;
    private String period; // DAILY, WEEKLY, MONTHLY
    private boolean anomaly; // Indicates if this data point is an outlier
}