package com.example.miapp.controller;

import com.example.miapp.dto.auth.MessageResponse;
import com.example.miapp.dto.dashboard.*;
import com.example.miapp.exception.DashboardValidationException;
import com.example.miapp.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para operaciones de dashboard
 * Proporciona endpoints REST para acceder a métricas, estadísticas y análisis
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Endpoint para obtener el dashboard principal del hospital
     * Accesible solo por administradores
     */
    @GetMapping("/hospital")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getHospitalDashboard(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Solicitud para obtener dashboard del hospital para fecha: {}", date);
        
        try {
            // Si no se proporciona fecha, usar la fecha actual
            LocalDate targetDate = (date != null) ? date : LocalDate.now();
            
            DashboardDto dashboard = dashboardService.getHospitalDashboard(targetDate);
            return ResponseEntity.ok(dashboard);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener dashboard: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener dashboard del hospital: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener dashboard: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener estadísticas en tiempo real
     * Accesible por administradores y médicos
     */
    @GetMapping("/real-time")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getRealTimeStats() {
        
        log.info("Solicitud para obtener estadísticas en tiempo real");
        
        try {
            RealTimeStatsDto stats = dashboardService.getRealTimeStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas en tiempo real: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener dashboard completo con KPIs y análisis
     * Accesible solo por administradores
     */
    @GetMapping("/comprehensive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getComprehensiveDashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Solicitud para obtener dashboard completo desde {} hasta {}", startDate, endDate);
        
        try {
            ComprehensiveDashboardDto dashboard = dashboardService.getComprehensiveDashboard(startDate, endDate);
            return ResponseEntity.ok(dashboard);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener dashboard completo: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener dashboard completo: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener dashboard: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener dashboard específico de un médico
     * Accesible por administradores y el propio médico
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentDoctor(#doctorId)")
    public ResponseEntity<?> getDoctorDashboard(
            @PathVariable Long doctorId,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Solicitud para obtener dashboard del doctor {} para fecha: {}", doctorId, date);
        
        try {
            // Si no se proporciona fecha, usar la fecha actual
            LocalDate targetDate = (date != null) ? date : LocalDate.now();
            
            DoctorDashboardDto dashboard = dashboardService.getDoctorDashboard(doctorId, targetDate);
            return ResponseEntity.ok(dashboard);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener dashboard del doctor: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener dashboard del doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener dashboard: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener dashboard específico de un paciente
     * Accesible por administradores, médicos y el propio paciente
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR') or @userSecurity.isCurrentPatient(#patientId)")
    public ResponseEntity<?> getPatientDashboard(@PathVariable Long patientId) {
        
        log.info("Solicitud para obtener dashboard del paciente {}", patientId);
        
        try {
            PatientDashboardDto dashboard = dashboardService.getPatientDashboard(patientId);
            return ResponseEntity.ok(dashboard);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener dashboard del paciente: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener dashboard del paciente {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener dashboard: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener dashboard financiero
     * Accesible solo por administradores
     */
    @GetMapping("/financial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getFinancialDashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Solicitud para obtener dashboard financiero desde {} hasta {}", startDate, endDate);
        
        try {
            FinancialDashboardDto dashboard = dashboardService.getFinancialDashboard(startDate, endDate);
            return ResponseEntity.ok(dashboard);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener dashboard financiero: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener dashboard financiero: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener dashboard: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener estadísticas de citas
     * Accesible por administradores y médicos
     */
    @GetMapping("/appointments/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getAppointmentStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Solicitud para obtener estadísticas de citas desde {} hasta {}", startDate, endDate);
        
        try {
            List<AppointmentStatusStatsDto> stats = dashboardService.getAppointmentStats(startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener estadísticas de citas: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de citas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener estadísticas por especialidad
     * Accesible por administradores y médicos
     */
    @GetMapping("/specialties/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getSpecialtyStats() {
        
        log.info("Solicitud para obtener estadísticas por especialidad");
        
        try {
            List<SpecialtyStatsDto> stats = dashboardService.getSpecialtyStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas por especialidad: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener estadísticas de médicos
     * Accesible solo por administradores
     */
    @GetMapping("/doctors/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDoctorStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {
        
        log.info("Solicitud para obtener estadísticas de médicos desde {} hasta {}", startDateTime, endDateTime);
        
        try {
            List<DoctorStatsDto> stats = dashboardService.getDoctorStats(startDateTime, endDateTime);
            return ResponseEntity.ok(stats);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener estadísticas de médicos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de médicos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener alertas del sistema
     * Accesible solo por administradores
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSystemAlerts() {
        
        log.info("Solicitud para obtener alertas del sistema");
        
        try {
            List<AlertDto> alerts = dashboardService.getSystemAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error al obtener alertas del sistema: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener alertas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener análisis de tendencias
     * Accesible solo por administradores
     */
    @GetMapping("/trends")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTrendAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Solicitud para obtener análisis de tendencias desde {} hasta {}", startDate, endDate);
        
        try {
            TrendAnalysisDto trends = dashboardService.getTrendAnalysis(startDate, endDate);
            return ResponseEntity.ok(trends);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener análisis de tendencias: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener análisis de tendencias: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener análisis: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener análisis predictivo
     * Accesible solo por administradores
     */
    @GetMapping("/predictions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPredictiveAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Solicitud para obtener análisis predictivo desde {} hasta {}", startDate, endDate);
        
        try {
            PredictiveAnalyticsDto predictions = dashboardService.getPredictiveAnalytics(startDate, endDate);
            return ResponseEntity.ok(predictions);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener análisis predictivo: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener análisis predictivo: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener análisis: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener benchmarks de rendimiento
     * Accesible solo por administradores
     */
    @GetMapping("/benchmarks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPerformanceBenchmarks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate previousDate) {
        
        log.info("Solicitud para obtener benchmarks comparando {} vs {}", currentDate, previousDate);
        
        try {
            PerformanceBenchmarkDto benchmarks = dashboardService.getPerformanceBenchmarks(currentDate, previousDate);
            return ResponseEntity.ok(benchmarks);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener benchmarks: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener benchmarks: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener benchmarks: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener eficiencia operacional
     * Accesible solo por administradores
     */
    @GetMapping("/efficiency")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getOperationalEfficiency(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Solicitud para obtener eficiencia operacional para fecha: {}", date);
        
        try {
            // Si no se proporciona fecha, usar la fecha actual
            LocalDate targetDate = (date != null) ? date : LocalDate.now();
            
            OperationalEfficiencyDto efficiency = dashboardService.getOperationalEfficiency(targetDate);
            return ResponseEntity.ok(efficiency);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener eficiencia operacional: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener eficiencia operacional: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener eficiencia: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener utilización de recursos
     * Accesible solo por administradores
     */
    @GetMapping("/utilization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getResourceUtilization(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Solicitud para obtener utilización de recursos desde {} hasta {}", startDate, endDate);
        
        try {
            ResourceUtilizationDto utilization = dashboardService.getResourceUtilization(startDate, endDate);
            return ResponseEntity.ok(utilization);
        } catch (DashboardValidationException e) {
            log.warn("Error de validación al obtener utilización de recursos: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener utilización de recursos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener utilización: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para validar acceso a un dashboard específico
     * Útil para validación en el frontend
     */
    @GetMapping("/access/validate")
    public ResponseEntity<?> validateDashboardAccess(
            @RequestParam String dashboardType,
            @RequestParam(required = false) Long userId) {
        
        log.info("Solicitud para validar acceso a dashboard: {}", dashboardType);
        
        try {
            // Obtener el rol del usuario actual (en una implementación real)
            String userRole = getCurrentUserRole(); // Método ficticio, en la implementación real vendría del contexto de seguridad
            
            boolean hasAccess = dashboardService.validateDashboardAccess(userRole, dashboardType, userId);
            
            if (hasAccess) {
                return ResponseEntity.ok(new MessageResponse("Acceso permitido al dashboard: " + dashboardType));
            } else {
                return ResponseEntity.status(403).body(
                        new MessageResponse("Acceso denegado al dashboard: " + dashboardType));
            }
        } catch (Exception e) {
            log.error("Error al validar acceso a dashboard: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al validar acceso: " + e.getMessage()));
        }
    }
    
    /**
     * Método para obtener el rol del usuario actual
     * Nota: Este método es ficticio, en una implementación real obtendría 
     * el rol del contexto de seguridad de Spring
     */
    private String getCurrentUserRole() {
        // En una implementación real, esto sería algo como:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // return auth.getAuthorities().stream().findFirst().orElse(null).getAuthority();
        
        return "ROLE_ADMIN"; // Valor ficticio para el ejemplo
    }
}