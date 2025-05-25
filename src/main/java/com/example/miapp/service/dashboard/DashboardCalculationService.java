package com.example.miapp.service.dashboard;

import com.example.miapp.dto.dashboard.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for dashboard calculations and metrics (Single Responsibility)
 * Implements Strategy Pattern for different calculation methods
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardCalculationService {

    /**
     * Calculates KPI metrics for dashboard
     */
    public Map<String, Object> calculateKPIs(DashboardStatsDto stats) {
        log.info("Calculating KPI metrics");
        
        Map<String, Object> kpis = new HashMap<>();
        
        // Appointment completion rate
        if (stats.getTotalAppointments() > 0) {
            double completionRate = (double) stats.getCompletedAppointments() / stats.getTotalAppointments() * 100;
            kpis.put("appointmentCompletionRate", Math.round(completionRate * 100.0) / 100.0);
        } else {
            kpis.put("appointmentCompletionRate", 0.0);
        }
        
        // No-show rate
        if (stats.getTotalAppointments() > 0) {
            double noShowRate = (double) stats.getNoShowAppointments() / stats.getTotalAppointments() * 100;
            kpis.put("noShowRate", Math.round(noShowRate * 100.0) / 100.0);
        } else {
            kpis.put("noShowRate", 0.0);
        }
        
        // Cancellation rate
        if (stats.getTotalAppointments() > 0) {
            double cancellationRate = (double) stats.getCanceledAppointments() / stats.getTotalAppointments() * 100;
            kpis.put("cancellationRate", Math.round(cancellationRate * 100.0) / 100.0);
        } else {
            kpis.put("cancellationRate", 0.0);
        }
        
        // Revenue per appointment
        if (stats.getTotalAppointments() > 0 && stats.getTotalRevenue() != null) {
            double revenuePerAppointment = stats.getTotalRevenue() / stats.getTotalAppointments();
            kpis.put("revenuePerAppointment", Math.round(revenuePerAppointment * 100.0) / 100.0);
        } else {
            kpis.put("revenuePerAppointment", 0.0);
        }
        
        // Patient growth rate (placeholder calculation)
        kpis.put("patientGrowthRate", calculatePatientGrowthRate(stats.getNewPatients(), stats.getTotalPatients()));
        
        return kpis;
    }

    /**
     * Calculates trend analysis for time series data
     */
    public TrendAnalysisDto calculateTrends(List<DashboardStatsDto> historicalData) {
        log.info("Calculating trend analysis for {} data points", historicalData.size());
        
        if (historicalData.size() < 2) {
            return new TrendAnalysisDto(); // Not enough data for trends
        }
        
        TrendAnalysisDto trends = new TrendAnalysisDto();
        
        // Calculate appointment trends
        trends.setAppointmentTrend(calculateLinearTrend(
            historicalData.stream()
                .map(DashboardStatsDto::getTotalAppointments)
                .map(Integer::doubleValue)
                .collect(Collectors.toList())
        ));
        
        // Calculate revenue trends
        trends.setRevenueTrend(calculateLinearTrend(
            historicalData.stream()
                .map(DashboardStatsDto::getTotalRevenue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        ));
        
        // Calculate patient growth trends
        trends.setPatientGrowthTrend(calculateLinearTrend(
            historicalData.stream()
                .map(DashboardStatsDto::getNewPatients)
                .map(Integer::doubleValue)
                .collect(Collectors.toList())
        ));
        
        return trends;
    }

    /**
     * Calculates performance benchmarks
     */
    public PerformanceBenchmarkDto calculateBenchmarks(DashboardStatsDto currentStats, DashboardStatsDto previousStats) {
        log.info("Calculating performance benchmarks");
        
        PerformanceBenchmarkDto benchmarks = new PerformanceBenchmarkDto();
        
        // Appointment completion benchmark
        if (previousStats.getTotalAppointments() > 0 && currentStats.getTotalAppointments() > 0) {
            double previousRate = (double) previousStats.getCompletedAppointments() / previousStats.getTotalAppointments();
            double currentRate = (double) currentStats.getCompletedAppointments() / currentStats.getTotalAppointments();
            benchmarks.setCompletionRateChange((currentRate - previousRate) * 100);
        }
        
        // Revenue benchmark
        if (previousStats.getTotalRevenue() != null && currentStats.getTotalRevenue() != null && previousStats.getTotalRevenue() > 0) {
            double revenueChange = ((currentStats.getTotalRevenue() - previousStats.getTotalRevenue()) / previousStats.getTotalRevenue()) * 100;
            benchmarks.setRevenueChange(revenueChange);
        }
        
        // Patient growth benchmark
        int patientGrowth = currentStats.getNewPatients() - previousStats.getNewPatients();
        benchmarks.setPatientGrowthChange(patientGrowth);
        
        return benchmarks;
    }

    /**
     * Calculates resource utilization metrics
     */
    public ResourceUtilizationDto calculateResourceUtilization(List<DoctorStatsDto> doctorStats, 
                                                             List<SpecialtyStatsDto> specialtyStats) {
        log.info("Calculating resource utilization");
        
        ResourceUtilizationDto utilization = new ResourceUtilizationDto();
        
        // Doctor utilization
        if (!doctorStats.isEmpty()) {
            double averageUtilization = doctorStats.stream()
                .mapToDouble(DoctorStatsDto::getCompletionRate)
                .average()
                .orElse(0.0);
            utilization.setAverageDoctorUtilization(averageUtilization);
            
            // Find most and least utilized doctors
            utilization.setMostUtilizedDoctor(
                doctorStats.stream()
                    .max(Comparator.comparing(DoctorStatsDto::getCompletionRate))
                    .map(DoctorStatsDto::getDoctorName)
                    .orElse("N/A")
            );
            
            utilization.setLeastUtilizedDoctor(
                doctorStats.stream()
                    .min(Comparator.comparing(DoctorStatsDto::getCompletionRate))
                    .map(DoctorStatsDto::getDoctorName)
                    .orElse("N/A")
            );
        }
        
        // Specialty demand
        if (!specialtyStats.isEmpty()) {
            utilization.setMostDemandedSpecialty(
                specialtyStats.stream()
                    .max(Comparator.comparing(SpecialtyStatsDto::getAppointmentCount))
                    .map(SpecialtyStatsDto::getSpecialtyName)
                    .orElse("N/A")
            );
            
            utilization.setLeastDemandedSpecialty(
                specialtyStats.stream()
                    .min(Comparator.comparing(SpecialtyStatsDto::getAppointmentCount))
                    .map(SpecialtyStatsDto::getSpecialtyName)
                    .orElse("N/A")
            );
        }
        
        return utilization;
    }

    /**
     * Calculates predictive analytics metrics
     */
    public PredictiveAnalyticsDto calculatePredictiveMetrics(List<DashboardStatsDto> historicalData) {
        log.info("Calculating predictive analytics");
        
        if (historicalData.size() < 3) {
            return new PredictiveAnalyticsDto(); // Not enough data for predictions
        }
        
        PredictiveAnalyticsDto predictions = new PredictiveAnalyticsDto();
        
        // Predict next period appointments
        List<Double> appointmentData = historicalData.stream()
            .map(DashboardStatsDto::getTotalAppointments)
            .map(Integer::doubleValue)
            .collect(Collectors.toList());
        
        predictions.setPredictedAppointments((int) Math.round(predictNextValue(appointmentData)));
        
        // Predict next period revenue
        List<Double> revenueData = historicalData.stream()
            .map(DashboardStatsDto::getTotalRevenue)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (!revenueData.isEmpty()) {
            predictions.setPredictedRevenue(predictNextValue(revenueData));
        }
        
        // Calculate confidence levels
        predictions.setConfidenceLevel(calculateConfidenceLevel(historicalData.size()));
        
        return predictions;
    }

    /**
     * Calculates operational efficiency metrics
     */
    public OperationalEfficiencyDto calculateOperationalEfficiency(DashboardStatsDto stats) {
        log.info("Calculating operational efficiency");
        
        OperationalEfficiencyDto efficiency = new OperationalEfficiencyDto();
        
        // Appointment efficiency
        if (stats.getTotalAppointments() > 0) {
            double efficiency_score = ((double) stats.getCompletedAppointments() / stats.getTotalAppointments()) * 100;
            efficiency.setAppointmentEfficiency(efficiency_score);
        }
        
        // Revenue efficiency (revenue per doctor)
        if (stats.getTotalDoctors() > 0 && stats.getTotalRevenue() != null) {
            double revenuePerDoctor = stats.getTotalRevenue() / stats.getTotalDoctors();
            efficiency.setRevenuePerDoctor(revenuePerDoctor);
        }
        
        // Patient throughput (patients per doctor)
        if (stats.getTotalDoctors() > 0) {
            double patientsPerDoctor = (double) stats.getTotalPatients() / stats.getTotalDoctors();
            efficiency.setPatientsPerDoctor(patientsPerDoctor);
        }
        
        // Calculate overall efficiency score
        efficiency.setOverallEfficiency(calculateOverallEfficiency(efficiency));
        
        return efficiency;
    }

    // Private helper methods

    private double calculatePatientGrowthRate(int newPatients, int totalPatients) {
        if (totalPatients == 0) return 0.0;
        return (double) newPatients / totalPatients * 100;
    }

    private double calculateLinearTrend(List<Double> data) {
        if (data.size() < 2) return 0.0;
        
        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i + 1; // Time index
            double y = data.get(i);
            
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        
        // Calculate slope (trend)
        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        return slope;
    }

    private double predictNextValue(List<Double> data) {
        if (data.size() < 2) return 0.0;
        
        double trend = calculateLinearTrend(data);
        double lastValue = data.get(data.size() - 1);
        
        // Simple linear prediction
        return lastValue + trend;
    }

    private double calculateConfidenceLevel(int dataPoints) {
        // Confidence increases with more data points
        if (dataPoints < 5) return 0.6;
        if (dataPoints < 10) return 0.75;
        if (dataPoints < 20) return 0.85;
        return 0.95;
    }

    private double calculateOverallEfficiency(OperationalEfficiencyDto efficiency) {
        double score = 0.0;
        int factors = 0;
        
        if (efficiency.getAppointmentEfficiency() > 0) {
            score += efficiency.getAppointmentEfficiency();
            factors++;
        }
        
        if (efficiency.getRevenuePerDoctor() > 0) {
            // Normalize revenue per doctor to 0-100 scale (assuming max 5000 per doctor)
            score += Math.min(efficiency.getRevenuePerDoctor() / 50, 100);
            factors++;
        }
        
        if (efficiency.getPatientsPerDoctor() > 0) {
            // Normalize patients per doctor to 0-100 scale (assuming max 200 per doctor)
            score += Math.min(efficiency.getPatientsPerDoctor() / 2, 100);
            factors++;
        }
        
        return factors > 0 ? score / factors : 0.0;
    }
}