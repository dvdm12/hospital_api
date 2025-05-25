package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for comprehensive dashboard with all analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveDashboardDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private DashboardStatsDto currentPeriodStats;
    private DashboardStatsDto previousPeriodStats;
    private Map<String, Object> kpis;
    private ResourceUtilizationDto resourceUtilization;
    private OperationalEfficiencyDto operationalEfficiency;
    private TrendAnalysisDto trendAnalysis;
    private PredictiveAnalyticsDto predictions;
    private PerformanceBenchmarkDto benchmarks;
}