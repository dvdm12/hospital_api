package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for financial dashboard with revenue analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialDashboardDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalRevenue;
    private Double previousPeriodRevenue;
    private Double averageRevenuePerAppointment;
    private Double averageRevenuePerPatient;
    private Map<String, Double> revenueBySpecialty;
    private Map<String, Double> revenueByDoctor;
    private Map<String, Double> revenueByDay;
    private double revenueGrowth;
    private double appointmentGrowth;
    private Double projectedRevenue;
    private int totalBilledAppointments;
    private Double outstandingPayments;
    private Double collectionRate;
    private String topRevenueSpecialty;
    private String topRevenueDoctor;
}