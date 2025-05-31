package com.example.miapp.controller;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.dto.auth.MessageResponse;
import com.example.miapp.exception.AppointmentValidationException;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.service.appointment.AppointmentBusinessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controlador para operaciones de citas
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentBusinessService appointmentService;

    /**
     * Endpoint para listar todas las citas
     * Solo accesible por administradores
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAppointments(
            @PageableDefault(size = 10, sort = "date") Pageable pageable) {
        
        log.info("Solicitud para listar todas las citas del sistema");
        
        try {
            Page<AppointmentDto> appointments = appointmentService.getAllAppointments(pageable);
            return ResponseEntity.ok(appointments);
        } catch (ResourceNotFoundException e) {
            log.warn("No se encontraron citas: {}", e.getMessage());
            return ResponseEntity.ok(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener las citas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener las citas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para listar citas de un día específico
     * Solo accesible por administradores
     */
    @GetMapping("/day")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAppointmentsByDay(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 10, sort = "date") Pageable pageable) {
        
        log.info("Solicitud para listar citas del día: {}", date != null ? date : "hoy");
        
        try {
            Page<AppointmentDto> appointments = appointmentService.getAppointmentsByDay(date, pageable);
            return ResponseEntity.ok(appointments);
        } catch (ResourceNotFoundException e) {
            log.warn("No se encontraron citas para el día indicado: {}", e.getMessage());
            return ResponseEntity.ok(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener las citas del día: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener las citas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para cancelar una cita
     * Solo accesible por administradores
     */
    @PostMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestParam(required = false) String reason) {
        
        log.info("Solicitud para cancelar cita con ID: {}", appointmentId);
        
        try {
            String cancelReason = (reason != null && !reason.isEmpty()) 
                    ? reason 
                    : "Cancelada por administrador del sistema";
            
            appointmentService.cancelAppointment(appointmentId, cancelReason);
            
            return ResponseEntity.ok(new MessageResponse("Cita cancelada exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Cita no encontrada: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (AppointmentValidationException e) {
            log.warn("Error de validación al cancelar cita: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al cancelar cita {}: {}", appointmentId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al cancelar la cita: " + e.getMessage()));
        }
    }
}