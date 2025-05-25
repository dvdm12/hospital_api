package com.example.miapp.service.dashboard;

import com.example.miapp.dto.dashboard.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Main business service that orchestrates dashboard operations (Facade Pattern)
 * Applies SOLID principles and Design Patterns
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DashboardBusinessService implements DashboardService {

    // Composed services following Single Responsibility
    private final DashboardValidationService validationService;
    private final DashboardQueryService queryService;
    private final DashboardCalculationService calculationService;

    /**
     * Gets complete hospital dashboard for a specific date
     */
    @Override
    public DashboardDto getHospitalDashboard(LocalDate date) {
        log.info("Getting hospital dashboard for date: {}", date);

        // Validate request
        validationService.validateDateRange(date, date);

        // Get basic stats
        DashboardStatsDto stats = queryService.getDashboardStats(date);

        // Build complete dashboard
        DashboardDto dashboard = new DashboardDto();
        dashboard.setDate(date);
        dashboard.setStats(stats);

        // Get detailed statistics
        dashboard.setAppointmentStats(queryService.getAppointmentStatusStats());
        dashboard.setSpecialtyStats(queryService.getSpecialtyStats());
        
        // Get doctor statistics for the date range
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(java.time.LocalTime.MAX);
        dashboard.setDoctorStats(queryService.getDoctorStats(startDateTime, endDateTime));

        // Get hourly and daily distributions
        dashboard.setAppointmentsByHour(queryService.getAppointmentsByHour(startDateTime, endDateTime));
        dashboard.setAppointmentsByDay(queryService.getAppointmentsByDayOfWeek(startDateTime, endDateTime));

        // Get system alerts
        dashboard.setAlerts(queryService.getSystemAlerts());

        log.info("Successfully retrieved hospital dashboard for {}", date);
        return dashboard;
    }

    /**
     * Gets real-time dashboard statistics
     */
    @Override
    public RealTimeStatsDto getRealTimeStats() {
        log.info("Getting real-time dashboard statistics");
        return queryService.getRealTimeStats();
    }

    /**
     * Gets comprehensive dashboard with KPIs and trends
     */
    @Override
    public ComprehensiveDashboardDto getComprehensiveDashboard(LocalDate startDate, LocalDate endDate) {
        log.info("Getting comprehensive dashboard from {} to {}", startDate, endDate);

        // Validate date range
        validationService.validateDateRange(startDate, endDate);

        ComprehensiveDashboardDto dashboard = new ComprehensiveDashboardDto();
        dashboard.setStartDate(startDate);
        dashboard.setEndDate(endDate);

        // Get current period stats
        DashboardStatsDto currentStats = queryService.getDashboardStats(endDate);
        dashboard.setCurrentPeriodStats(currentStats);

        // Calculate KPIs
        Map<String, Object> kpis = calculationService.calculateKPIs(currentStats);
        dashboard.setKpis(kpis);

        // Get resource utilization
        List<DoctorStatsDto> doctorStats = queryService.getDoctorStats(
            startDate.atStartOfDay(), 
            endDate.atTime(java.time.LocalTime.MAX)
        );
        List<SpecialtyStatsDto> specialtyStats = queryService.getSpecialtyStats();
        
        ResourceUtilizationDto utilization = calculationService.calculateResourceUtilization(
            doctorStats, specialtyStats);
        dashboard.setResourceUtilization(utilization);

        // Calculate operational efficiency
        OperationalEfficiencyDto efficiency = calculationService.calculateOperationalEfficiency(currentStats);
        dashboard.setOperationalEfficiency(efficiency);

        return dashboard;
    }

    /**
     * Gets doctor-specific dashboard
     */
    @Override
    public DoctorDashboardDto getDoctorDashboard(Long doctorId, LocalDate date) {
        log.info("Getting doctor dashboard for doctor {} on {}", doctorId, date);

        // Validate inputs
        validationService.validateDoctorId(doctorId);
        validationService.validateDateRange(date, date);

        return queryService.getDoctorDashboard(doctorId, date);
    }

    /**
     * Gets patient-specific dashboard
     */
    @Override
    public PatientDashboardDto getPatientDashboard(Long patientId) {
        log.info("Getting patient dashboard for patient {}", patientId);

        // Validate input
        validationService.validatePatientId(patientId);

        return queryService.getPatientDashboard(patientId);
    }

    /**
     * Gets financial dashboard with revenue analytics
     */
    @Override
    public FinancialDashboardDto getFinancialDashboard(LocalDate startDate, LocalDate endDate) {
        log.info("Getting financial dashboard from {} to {}", startDate, endDate);

        // Validate date range
        validationService.validateDateRange(startDate, endDate);

        return queryService.getFinancialDashboard(startDate, endDate);
    }

    /**
     * Gets appointment statistics for a date range
     */
    @Override
    public List<AppointmentStatusStatsDto> getAppointmentStats(LocalDate startDate, LocalDate endDate) {
        log.info("Getting appointment statistics from {} to {}", startDate, endDate);

        // Validate date range
        validationService.validateDateRange(startDate, endDate);

        return queryService.getAppointmentStatusStats();
    }

    /**
     * Gets specialty performance statistics
     */
    @Override
    public List<SpecialtyStatsDto> getSpecialtyStats() {
        log.info("Getting specialty statistics");
        return queryService.getSpecialtyStats();
    }

    /**
     * Gets doctor performance statistics
     */
    @Override
    public List<DoctorStatsDto> getDoctorStats(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting doctor statistics from {} to {}", startDate, endDate);

        // Validate datetime range
        validationService.validateDateTimeRange(startDate, endDate);

        return queryService.getDoctorStats(startDate, endDate);
    }

    /**
     * Gets system alerts and notifications
     */
    @Override
    public List<AlertDto> getSystemAlerts() {
        log.info("Getting system alerts");
        return queryService.getSystemAlerts();
    }

    /**
     * Gets trend analysis for historical data
     */
    @Override
    public TrendAnalysisDto getTrendAnalysis(LocalDate startDate, LocalDate endDate) {
        log.info("Getting trend analysis from {} to {}", startDate, endDate);

        // Validate date range
        validationService.validateDateRange(startDate, endDate);

        // Get historical data (placeholder - would need to implement historical data collection)
        List<DashboardStatsDto> historicalData = getHistoricalData(startDate, endDate);

        return calculationService.calculateTrends(historicalData);
    }

    /**
     * Gets predictive analytics
     */
    @Override
    public PredictiveAnalyticsDto getPredictiveAnalytics(LocalDate startDate, LocalDate endDate) {
        log.info("Getting predictive analytics from {} to {}", startDate, endDate);

        // Validate date range
        validationService.validateDateRange(startDate, endDate);

        // Get historical data for predictions
        List<DashboardStatsDto> historicalData = getHistoricalData(startDate, endDate);

        return calculationService.calculatePredictiveMetrics(historicalData);
    }

    /**
     * Gets performance benchmarks comparing current vs previous period
     */
    @Override
    public PerformanceBenchmarkDto getPerformanceBenchmarks(LocalDate currentDate, LocalDate previousDate) {
        log.info("Getting performance benchmarks comparing {} vs {}", currentDate, previousDate);

        // Validate dates
        validationService.validateDateRange(previousDate, currentDate);

        DashboardStatsDto currentStats = queryService.getDashboardStats(currentDate);
        DashboardStatsDto previousStats = queryService.getDashboardStats(previousDate);

        return calculationService.calculateBenchmarks(currentStats, previousStats);
    }

    /**
     * Gets operational efficiency metrics
     */
    @Override
    public OperationalEfficiencyDto getOperationalEfficiency(LocalDate date) {
        log.info("Getting operational efficiency for {}", date);

        // Validate date
        validationService.validateDateRange(date, date);

        DashboardStatsDto stats = queryService.getDashboardStats(date);
        return calculationService.calculateOperationalEfficiency(stats);
    }

    /**
     * Gets resource utilization analysis
     */
    @Override
    public ResourceUtilizationDto getResourceUtilization(LocalDate startDate, LocalDate endDate) {
        log.info("Getting resource utilization from {} to {}", startDate, endDate);

        // Validate date range
        validationService.validateDateRange(startDate, endDate);

        List<DoctorStatsDto> doctorStats = queryService.getDoctorStats(
            startDate.atStartOfDay(), 
            endDate.atTime(java.time.LocalTime.MAX)
        );
        List<SpecialtyStatsDto> specialtyStats = queryService.getSpecialtyStats();

        return calculationService.calculateResourceUtilization(doctorStats, specialtyStats);
    }

    /**
     * Validates dashboard access based on user role
     */
    @Override
    public boolean validateDashboardAccess(String userRole, String dashboardType, Long userId) {
        log.info("Validating dashboard access for role {} to {} dashboard", userRole, dashboardType);

        try {
            validationService.validateDashboardAccess(userRole, dashboardType);
            
            // Additional validation for specific dashboard types
            if ("PATIENT".equalsIgnoreCase(dashboardType) && "ROLE_PATIENT".equals(userRole)) {
                // Patients can only access their own dashboard
                return userId != null;
            }
            
            return true;
        } catch (Exception e) {
            log.warn("Dashboard access validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Private helper methods

    private List<DashboardStatsDto> getHistoricalData(LocalDate startDate, LocalDate endDate) {
        // This would typically fetch historical dashboard statistics
        // For now, we'll create some sample data
        List<DashboardStatsDto> historicalData = new java.util.ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DashboardStatsDto stats = queryService.getDashboardStats(current);
            historicalData.add(stats);
            current = current.plusDays(1);
        }
        
        return historicalData;
    }
}