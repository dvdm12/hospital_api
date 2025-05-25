package com.example.miapp.service.dashboard;

import com.example.miapp.dto.dashboard.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface Segregation Principle - Clean contract for dashboard operations
 * Dependency Inversion Principle - High-level modules depend on abstractions
 */
public interface DashboardService {

    // Main Dashboard Operations
    DashboardDto getHospitalDashboard(LocalDate date);
    RealTimeStatsDto getRealTimeStats();
    ComprehensiveDashboardDto getComprehensiveDashboard(LocalDate startDate, LocalDate endDate);

    // Specialized Dashboards
    DoctorDashboardDto getDoctorDashboard(Long doctorId, LocalDate date);
    PatientDashboardDto getPatientDashboard(Long patientId);
    FinancialDashboardDto getFinancialDashboard(LocalDate startDate, LocalDate endDate);

    // Statistics Operations
    List<AppointmentStatusStatsDto> getAppointmentStats(LocalDate startDate, LocalDate endDate);
    List<SpecialtyStatsDto> getSpecialtyStats();
    List<DoctorStatsDto> getDoctorStats(LocalDateTime startDate, LocalDateTime endDate);
    List<AlertDto> getSystemAlerts();

    // Analytics Operations
    TrendAnalysisDto getTrendAnalysis(LocalDate startDate, LocalDate endDate);
    PredictiveAnalyticsDto getPredictiveAnalytics(LocalDate startDate, LocalDate endDate);
    PerformanceBenchmarkDto getPerformanceBenchmarks(LocalDate currentDate, LocalDate previousDate);

    // Efficiency Operations
    OperationalEfficiencyDto getOperationalEfficiency(LocalDate date);
    ResourceUtilizationDto getResourceUtilization(LocalDate startDate, LocalDate endDate);

    // Security Operations
    boolean validateDashboardAccess(String userRole, String dashboardType, Long userId);
}