package com.example.miapp.service.dashboard;

import com.example.miapp.exception.DashboardValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service responsible for dashboard validations (Single Responsibility)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardValidationService {

    /**
     * Validates date range for dashboard queries
     */
    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new DashboardValidationException("Start date and end date are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new DashboardValidationException("Start date cannot be after end date");
        }
        
        if (startDate.isAfter(LocalDate.now())) {
            throw new DashboardValidationException("Start date cannot be in the future");
        }
        
        // Validate reasonable date range (not more than 5 years)
        if (startDate.isBefore(LocalDate.now().minusYears(5))) {
            log.warn("Dashboard query for very old data (more than 5 years): {}", startDate);
        }
        
        // Validate range is not too large (performance consideration)
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > 365) {
            log.warn("Dashboard query for large date range ({} days): {} to {}", daysBetween, startDate, endDate);
        }
    }

    /**
     * Validates datetime range for dashboard queries
     */
    public void validateDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            throw new DashboardValidationException("Start datetime and end datetime are required");
        }
        
        if (startDateTime.isAfter(endDateTime)) {
            throw new DashboardValidationException("Start datetime cannot be after end datetime");
        }
        
        if (startDateTime.isAfter(LocalDateTime.now())) {
            throw new DashboardValidationException("Start datetime cannot be in the future");
        }
    }

    /**
     * Validates doctor ID for doctor-specific dashboard
     */
    public void validateDoctorId(Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new DashboardValidationException("Valid doctor ID is required");
        }
    }

    /**
     * Validates patient ID for patient-specific dashboard
     */
    public void validatePatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new DashboardValidationException("Valid patient ID is required");
        }
    }

    /**
     * Validates specialty ID for specialty-specific dashboard
     */
    public void validateSpecialtyId(Long specialtyId) {
        if (specialtyId == null || specialtyId <= 0) {
            throw new DashboardValidationException("Valid specialty ID is required");
        }
    }

    /**
     * Validates pagination parameters
     */
    public void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new DashboardValidationException("Page number cannot be negative");
        }
        
        if (size <= 0) {
            throw new DashboardValidationException("Page size must be positive");
        }
        
        if (size > 1000) {
            throw new DashboardValidationException("Page size cannot exceed 1000 for performance reasons");
        }
    }

    /**
     * Validates metrics calculation parameters
     */
    public void validateMetricsParams(String metricType, Object... params) {
        if (metricType == null || metricType.trim().isEmpty()) {
            throw new DashboardValidationException("Metric type is required");
        }
        
        // Validate specific metric types
        switch (metricType.toUpperCase()) {
            case "REVENUE":
                validateRevenueParams(params);
                break;
            case "APPOINTMENT_RATE":
                validateAppointmentRateParams(params);
                break;
            case "PATIENT_SATISFACTION":
                validateSatisfactionParams(params);
                break;
            case "DOCTOR_UTILIZATION":
                validateUtilizationParams(params);
                break;
            default:
                log.warn("Unknown metric type requested: {}", metricType);
        }
    }

    /**
     * Validates dashboard access permissions
     */
    public void validateDashboardAccess(String userRole, String dashboardType) {
        if (userRole == null || userRole.trim().isEmpty()) {
            throw new DashboardValidationException("User role is required for dashboard access");
        }
        
        if (dashboardType == null || dashboardType.trim().isEmpty()) {
            throw new DashboardValidationException("Dashboard type is required");
        }
        
        // Validate role-based access
        switch (dashboardType.toUpperCase()) {
            case "ADMIN":
                validateAdminDashboardAccess(userRole);
                break;
            case "DOCTOR":
                validateDoctorDashboardAccess(userRole);
                break;
            case "PATIENT":
                validatePatientDashboardAccess(userRole);
                break;
            case "FINANCIAL":
                validateFinancialDashboardAccess(userRole);
                break;
            default:
                throw new DashboardValidationException("Unknown dashboard type: " + dashboardType);
        }
    }

    /**
     * Validates time period for statistics
     */
    public void validateTimePeriod(String period) {
        if (period == null || period.trim().isEmpty()) {
            throw new DashboardValidationException("Time period is required");
        }
        
        String[] validPeriods = {"TODAY", "WEEK", "MONTH", "QUARTER", "YEAR", "CUSTOM"};
        boolean isValid = false;
        
        for (String validPeriod : validPeriods) {
            if (validPeriod.equalsIgnoreCase(period.trim())) {
                isValid = true;
                break;
            }
        }
        
        if (!isValid) {
            throw new DashboardValidationException("Invalid time period. Valid periods: TODAY, WEEK, MONTH, QUARTER, YEAR, CUSTOM");
        }
    }

    // Private validation methods

    private void validateRevenueParams(Object... params) {
        // Revenue calculations might need specific parameters
        if (params.length > 0 && params[0] instanceof Double) {
            Double expectedRevenue = (Double) params[0];
            if (expectedRevenue < 0) {
                throw new DashboardValidationException("Expected revenue cannot be negative");
            }
        }
    }

    private void validateAppointmentRateParams(Object... params) {
        // Appointment rate calculations
        if (params.length > 0 && params[0] instanceof Integer) {
            Integer targetRate = (Integer) params[0];
            if (targetRate < 0 || targetRate > 100) {
                throw new DashboardValidationException("Appointment rate must be between 0 and 100");
            }
        }
    }

    private void validateSatisfactionParams(Object... params) {
        // Patient satisfaction parameters
        if (params.length > 0 && params[0] instanceof Double) {
            Double rating = (Double) params[0];
            if (rating < 1.0 || rating > 5.0) {
                throw new DashboardValidationException("Satisfaction rating must be between 1.0 and 5.0");
            }
        }
    }

    private void validateUtilizationParams(Object... params) {
        // Doctor utilization parameters
        if (params.length > 0 && params[0] instanceof Integer) {
            Integer hoursPerWeek = (Integer) params[0];
            if (hoursPerWeek < 0 || hoursPerWeek > 168) { // 168 hours in a week
                throw new DashboardValidationException("Hours per week must be between 0 and 168");
            }
        }
    }

    private void validateAdminDashboardAccess(String userRole) {
        if (!userRole.contains("ADMIN")) {
            throw new DashboardValidationException("Admin dashboard access requires admin role");
        }
    }

    private void validateDoctorDashboardAccess(String userRole) {
        if (!userRole.contains("DOCTOR") && !userRole.contains("ADMIN")) {
            throw new DashboardValidationException("Doctor dashboard access requires doctor or admin role");
        }
    }

    private void validatePatientDashboardAccess(String userRole) {
        // Patients can access their own dashboard, doctors and admins can access any
        if (!userRole.contains("PATIENT") && !userRole.contains("DOCTOR") && !userRole.contains("ADMIN")) {
            throw new DashboardValidationException("Insufficient permissions for patient dashboard access");
        }
    }

    private void validateFinancialDashboardAccess(String userRole) {
        if (!userRole.contains("ADMIN") && !userRole.contains("FINANCIAL")) {
            throw new DashboardValidationException("Financial dashboard access requires admin or financial role");
        }
    }
}