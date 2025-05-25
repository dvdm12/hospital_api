package com.example.miapp.service.dashboard;

import com.example.miapp.dto.dashboard.*;
import com.example.miapp.entity.Appointment.AppointmentStatus;
import com.example.miapp.mapper.DashboardMapper;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.PatientRepository;
import com.example.miapp.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for dashboard queries and data aggregation (Single Responsibility)
 * Implements Repository Pattern and Aggregator Pattern
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DashboardQueryService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DashboardMapper dashboardMapper;

    /**
     * Gets main dashboard statistics for a specific date
     */
    public DashboardStatsDto getDashboardStats(LocalDate date) {
        log.info("Getting dashboard stats for date: {}", date);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        DashboardStatsDto stats = new DashboardStatsDto();
        
        // Get appointment statistics
        List<Object[]> appointmentStats = appointmentRepository.countAppointmentsByStatus();
        populateAppointmentStats(stats, appointmentStats, startOfDay, endOfDay);
        
        // Get patient counts
        stats.setTotalPatients((int) patientRepository.count());
        stats.setNewPatients(getNewPatientsCount(date));
        
        // Get doctor count
        stats.setTotalDoctors((int) doctorRepository.count());
        
        // Calculate revenue and other metrics
        stats.setTotalRevenue(calculateTotalRevenue(startOfDay, endOfDay));
        stats.setAverageAppointmentDuration(calculateAverageAppointmentDuration(startOfDay, endOfDay));
        
        return stats;
    }

    /**
     * Gets appointment status statistics
     */
    public List<AppointmentStatusStatsDto> getAppointmentStatusStats() {
        log.info("Getting appointment status statistics");
        
        List<Object[]> results = appointmentRepository.countAppointmentsByStatus();
        return dashboardMapper.mapToAppointmentStatusStatsList(results);
    }

    /**
     * Gets specialty statistics
     */
    public List<SpecialtyStatsDto> getSpecialtyStats() {
        log.info("Getting specialty statistics");
        
        List<Object[]> results = specialtyRepository.countDoctorsBySpecialty();
        return dashboardMapper.mapToSpecialtyStatsList(results);
    }

    /**
     * Gets doctor statistics for a date range
     */
    public List<DoctorStatsDto> getDoctorStats(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting doctor statistics from {} to {}", startDate, endDate);
        
        List<Object[]> results = doctorRepository.countAppointmentsByDoctor(startDate, endDate);
        return dashboardMapper.mapToDoctorStatsList(results);
    }

    /**
     * Gets appointments by hour statistics
     */
    public Map<String, Integer> getAppointmentsByHour(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting appointments by hour from {} to {}", startDate, endDate);
        
        List<Object[]> results = appointmentRepository.countAppointmentsByHourOfDay(startDate, endDate);
        return dashboardMapper.mapToHourlyStats(results);
    }

    /**
     * Gets appointments by day of week statistics
     */
    public Map<String, Integer> getAppointmentsByDayOfWeek(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting appointments by day of week from {} to {}", startDate, endDate);
        
        List<Object[]> results = appointmentRepository.countAppointmentsByDayOfWeek(startDate, endDate);
        return dashboardMapper.mapToHourlyStats(results); // Same mapping logic
    }

    /**
     * Gets real-time dashboard statistics
     */
    public RealTimeStatsDto getRealTimeStats() {
        log.info("Getting real-time dashboard statistics");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        
        RealTimeStatsDto stats = new RealTimeStatsDto();
        stats.setCurrentTime(now);
        stats.setTodayAppointments(getTodayAppointmentCount());
        stats.setActivePatients(getActivePatientsCount());
        stats.setOnlineDoctors(getOnlineDoctorsCount());
        stats.setPendingAppointments(getPendingAppointmentsCount());
        stats.setTodayRevenue(calculateTotalRevenue(startOfDay, now));
        
        return stats;
    }

    /**
     * Gets doctor-specific dashboard data
     */
    public DoctorDashboardDto getDoctorDashboard(Long doctorId, LocalDate date) {
        log.info("Getting doctor dashboard for doctor {} on {}", doctorId, date);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        DoctorDashboardDto dashboard = new DoctorDashboardDto();
        dashboard.setDoctorId(doctorId);
        dashboard.setDate(date);
        
        // Get doctor's appointments for the day
        dashboard.setTodayAppointments(getDoctorTodayAppointments(doctorId));
        dashboard.setTotalAppointments(getDoctorAppointmentCount(doctorId, startOfDay, endOfDay));
        dashboard.setCompletedAppointments(getDoctorCompletedAppointmentCount(doctorId, startOfDay, endOfDay));
        dashboard.setCanceledAppointments(getDoctorCanceledAppointmentCount(doctorId, startOfDay, endOfDay));
        
        // Calculate utilization
        dashboard.setUtilizationRate(calculateDoctorUtilization(doctorId, date));
        
        // Get upcoming appointments
        dashboard.setUpcomingAppointments(getDoctorUpcomingAppointments(doctorId));
        
        return dashboard;
    }

    /**
     * Gets patient-specific dashboard data
     */
    public PatientDashboardDto getPatientDashboard(Long patientId) {
        log.info("Getting patient dashboard for patient {}", patientId);
        
        PatientDashboardDto dashboard = new PatientDashboardDto();
        dashboard.setPatientId(patientId);
        
        // Get patient's upcoming appointments
        dashboard.setUpcomingAppointments(getPatientUpcomingAppointments(patientId));
        
        // Get recent medical entries
        dashboard.setRecentMedicalEntries(getPatientRecentMedicalEntries(patientId));
        
        // Get active prescriptions
        dashboard.setActivePrescriptions(getPatientActivePrescriptions(patientId));
        
        // Get appointment history summary
        dashboard.setAppointmentHistory(getPatientAppointmentHistory(patientId));
        
        return dashboard;
    }

    /**
     * Gets system alerts and notifications
     */
    public List<AlertDto> getSystemAlerts() {
        log.info("Getting system alerts");
        
        List<AlertDto> alerts = new ArrayList<>();
        
        // Check for overdue appointments
        alerts.addAll(getOverdueAppointmentAlerts());
        
        // Check for system performance issues
        alerts.addAll(getPerformanceAlerts());
        
        // Check for capacity issues
        alerts.addAll(getCapacityAlerts());
        
        // Check for data integrity issues
        alerts.addAll(getDataIntegrityAlerts());
        
        return alerts;
    }

    /**
     * Gets financial dashboard data
     */
    public FinancialDashboardDto getFinancialDashboard(LocalDate startDate, LocalDate endDate) {
        log.info("Getting financial dashboard from {} to {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        FinancialDashboardDto dashboard = new FinancialDashboardDto();
        dashboard.setStartDate(startDate);
        dashboard.setEndDate(endDate);
        
        // Calculate revenue metrics
        dashboard.setTotalRevenue(calculateTotalRevenue(startDateTime, endDateTime));
        dashboard.setAverageRevenuePerAppointment(calculateAverageRevenuePerAppointment(startDateTime, endDateTime));
        dashboard.setRevenueBySpecialty(getRevenueBySpecialty(startDateTime, endDateTime));
        dashboard.setRevenueByDoctor(getRevenueByDoctor(startDateTime, endDateTime));
        
        // Calculate growth metrics
        dashboard.setRevenueGrowth(calculateRevenueGrowth(startDate, endDate));
        dashboard.setAppointmentGrowth(calculateAppointmentGrowth(startDate, endDate));
        
        return dashboard;
    }

    // Private helper methods for calculations

    private void populateAppointmentStats(DashboardStatsDto stats, List<Object[]> appointmentStats, 
                                        LocalDateTime startOfDay, LocalDateTime endOfDay) {
        Map<AppointmentStatus, Long> statusCounts = new HashMap<>();
        
        for (Object[] stat : appointmentStats) {
            AppointmentStatus status = (AppointmentStatus) stat[0];
            Long count = ((Number) stat[1]).longValue();
            statusCounts.put(status, count);
        }
        
        stats.setTotalAppointments(statusCounts.values().stream().mapToInt(Long::intValue).sum());
        stats.setScheduledAppointments(statusCounts.getOrDefault(AppointmentStatus.SCHEDULED, 0L).intValue());
        stats.setCompletedAppointments(statusCounts.getOrDefault(AppointmentStatus.COMPLETED, 0L).intValue());
        stats.setCanceledAppointments(statusCounts.getOrDefault(AppointmentStatus.CANCELED, 0L).intValue());
        stats.setNoShowAppointments(statusCounts.getOrDefault(AppointmentStatus.NO_SHOW, 0L).intValue());
    }

    private int getNewPatientsCount(LocalDate date) {
        // This would typically use a more sophisticated query
        // For now, we'll use a simple estimation
        return 5; // Placeholder
    }

    private Double calculateTotalRevenue(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // This would calculate revenue based on completed appointments and consultation fees
        // Placeholder implementation
        return 15000.0;
    }

    private Double calculateAverageAppointmentDuration(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Calculate average appointment duration in minutes
        // Placeholder implementation
        return 30.0;
    }

    private int getTodayAppointmentCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return (int) appointmentRepository.findByDateBetween(startOfDay, endOfDay, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    private int getActivePatientsCount() {
        // Count patients with appointments in the last 30 days
        return 150; // Placeholder
    }

    private int getOnlineDoctorsCount() {
        // Count doctors who are currently active/available
        return 12; // Placeholder
    }

    private int getPendingAppointmentsCount() {
        return (int) appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    private List<AppointmentSummaryDto> getDoctorTodayAppointments(Long doctorId) {
        // Get today's appointments for specific doctor
        return new ArrayList<>(); // Placeholder
    }

    private int getDoctorAppointmentCount(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return (int) appointmentRepository.findByDoctorIdAndDateBetween(doctorId, start, end, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    private int getDoctorCompletedAppointmentCount(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return (int) appointmentRepository.findByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    private int getDoctorCanceledAppointmentCount(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return (int) appointmentRepository.findByDoctorIdAndStatus(doctorId, AppointmentStatus.CANCELED, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
    }

    private double calculateDoctorUtilization(Long doctorId, LocalDate date) {
        // Calculate utilization based on scheduled vs available time
        return 0.75; // Placeholder - 75% utilization
    }

    private List<AppointmentSummaryDto> getDoctorUpcomingAppointments(Long doctorId) {
        return new ArrayList<>(); // Placeholder
    }

    private List<AppointmentSummaryDto> getPatientUpcomingAppointments(Long patientId) {
        return new ArrayList<>(); // Placeholder
    }

    private List<MedicalEntrySummaryDto> getPatientRecentMedicalEntries(Long patientId) {
        return new ArrayList<>(); // Placeholder
    }

    private List<PrescriptionSummaryDto> getPatientActivePrescriptions(Long patientId) {
        return new ArrayList<>(); // Placeholder
    }

    private AppointmentHistorySummaryDto getPatientAppointmentHistory(Long patientId) {
        return new AppointmentHistorySummaryDto(); // Placeholder
    }

    private List<AlertDto> getOverdueAppointmentAlerts() {
        List<AlertDto> alerts = new ArrayList<>();
        // Check for appointments that should be marked as no-show
        alerts.add(new AlertDto("OVERDUE_APPOINTMENTS", "5 appointments are overdue", "WARNING", LocalDateTime.now(), "Mark as no-show"));
        return alerts;
    }

    private List<AlertDto> getPerformanceAlerts() {
        List<AlertDto> alerts = new ArrayList<>();
        // Check system performance metrics
        return alerts; // Placeholder
    }

    private List<AlertDto> getCapacityAlerts() {
        List<AlertDto> alerts = new ArrayList<>();
        // Check for capacity issues
        alerts.add(new AlertDto("HIGH_CAPACITY", "Doctor schedules are 90% full this week", "INFO", LocalDateTime.now(), "Consider adding more slots"));
        return alerts;
    }

    private List<AlertDto> getDataIntegrityAlerts() {
        List<AlertDto> alerts = new ArrayList<>();
        // Check for data integrity issues
        return alerts; // Placeholder
    }

    private Double calculateAverageRevenuePerAppointment(LocalDateTime start, LocalDateTime end) {
        return 250.0; // Placeholder
    }

    private Map<String, Double> getRevenueBySpecialty(LocalDateTime start, LocalDateTime end) {
        Map<String, Double> revenue = new HashMap<>();
        revenue.put("Cardiology", 5000.0);
        revenue.put("Neurology", 3500.0);
        revenue.put("General", 2000.0);
        return revenue;
    }

    private Map<String, Double> getRevenueByDoctor(LocalDateTime start, LocalDateTime end) {
        Map<String, Double> revenue = new HashMap<>();
        revenue.put("Dr. Smith", 3000.0);
        revenue.put("Dr. Johnson", 2500.0);
        revenue.put("Dr. Williams", 2000.0);
        return revenue;
    }

    private double calculateRevenueGrowth(LocalDate startDate, LocalDate endDate) {
        return 0.15; // Placeholder - 15% growth
    }

    private double calculateAppointmentGrowth(LocalDate startDate, LocalDate endDate) {
        return 0.12; // Placeholder - 12% growth
    }
}