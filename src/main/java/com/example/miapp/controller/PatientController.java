package com.example.miapp.controller;

import com.example.miapp.dto.auth.MessageResponse;
import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.dto.patient.PatientDto;
import com.example.miapp.dto.patient.PatientSearchCriteria;
import com.example.miapp.entity.Patient.Gender;
import com.example.miapp.exception.PatientValidationException;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.service.patient.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador para operaciones de pacientes
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;

    /**
     * Endpoint para listar todos los pacientes
     * Solo accesible por administradores y médicos
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getAllPatients(
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para listar todos los pacientes");
        
        try {
            Page<PatientDto> patients = patientService.getAllPatients(pageable);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error al obtener pacientes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener pacientes: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener un paciente por ID
     * Accesible por administradores y médicos
     */
    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getPatientById(@PathVariable Long patientId) {
        
        log.info("Solicitud para obtener paciente con ID: {}", patientId);
        
        try {
            PatientDto patient = patientService.getPatient(patientId);
            return ResponseEntity.ok(patient);
        } catch (ResourceNotFoundException e) {
            log.warn("Paciente no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener paciente {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener paciente: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para crear un nuevo paciente
     * Accesible por administradores
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        
        log.info("Solicitud para crear nuevo paciente: {} {}", request.getFirstName(), request.getLastName());
        
        try {
            PatientDto createdPatient = patientService.createPatient(request);
            return ResponseEntity.ok(createdPatient);
        } catch (PatientValidationException e) {
            log.warn("Error de validación al crear paciente: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear paciente: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al crear paciente: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar un paciente existente
     * Accesible por administradores
     */
    @PutMapping("/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePatient(
            @PathVariable Long patientId,
            @Valid @RequestBody CreatePatientRequest request) {
        
        log.info("Solicitud para actualizar paciente con ID: {}", patientId);
        
        try {
            PatientDto updatedPatient = patientService.updatePatient(patientId, request);
            return ResponseEntity.ok(updatedPatient);
        } catch (ResourceNotFoundException e) {
            log.warn("Paciente no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (PatientValidationException e) {
            log.warn("Error de validación al actualizar paciente: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar paciente {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al actualizar paciente: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para eliminar un paciente
     * Accesible solo por administradores
     */
    @DeleteMapping("/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePatient(@PathVariable Long patientId) {
        
        log.info("Solicitud para eliminar paciente con ID: {}", patientId);
        
        try {
            patientService.deletePatient(patientId);
            return ResponseEntity.ok(new MessageResponse("Paciente eliminado exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Paciente no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (PatientValidationException e) {
            log.warn("Error de validación al eliminar paciente: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al eliminar paciente {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al eliminar paciente: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para archivar un paciente
     * Accesible solo por administradores
     */
    @PostMapping("/{patientId}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> archivePatient(
            @PathVariable Long patientId,
            @RequestParam(required = false) String reason) {
        
        log.info("Solicitud para archivar paciente con ID: {}", patientId);
        
        try {
            String archiveReason = (reason != null && !reason.isEmpty()) 
                    ? reason 
                    : "Archivado por administrador del sistema";
            
            patientService.archivePatient(patientId, archiveReason);
            
            return ResponseEntity.ok(new MessageResponse("Paciente archivado exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Paciente no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (PatientValidationException e) {
            log.warn("Error de validación al archivar paciente: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al archivar paciente {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al archivar paciente: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar la información de contacto de un paciente
     * Accesible por administradores y el propio paciente
     */
    @PatchMapping("/{patientId}/contact")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#patientId)")
    public ResponseEntity<?> updatePatientContact(
            @PathVariable Long patientId,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address) {
        
        log.info("Solicitud para actualizar información de contacto del paciente con ID: {}", patientId);
        
        try {
            patientService.updatePatientProfile(patientId, null, null, phone, address);
            return ResponseEntity.ok(new MessageResponse("Información de contacto actualizada exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Paciente no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (PatientValidationException e) {
            log.warn("Error de validación al actualizar contacto: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar contacto del paciente {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al actualizar información de contacto: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar la información de seguro médico de un paciente
     * Accesible solo por administradores
     */
    @PatchMapping("/{patientId}/insurance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePatientInsurance(
            @PathVariable Long patientId,
            @RequestParam String insuranceProvider,
            @RequestParam String insurancePolicyNumber) {
        
        log.info("Solicitud para actualizar información de seguro del paciente con ID: {}", patientId);
        
        try {
            patientService.updateInsuranceInfo(patientId, insuranceProvider, insurancePolicyNumber);
            return ResponseEntity.ok(new MessageResponse("Información de seguro actualizada exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Paciente no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (PatientValidationException e) {
            log.warn("Error de validación al actualizar seguro: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar seguro del paciente {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al actualizar información de seguro: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar la información de contacto de emergencia de un paciente
     * Accesible por administradores y el propio paciente
     */
    @PatchMapping("/{patientId}/emergency-contact")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#patientId)")
    public ResponseEntity<?> updatePatientEmergencyContact(
            @PathVariable Long patientId,
            @RequestParam String emergencyContactName,
            @RequestParam String emergencyContactPhone) {
        
        log.info("Solicitud para actualizar contacto de emergencia del paciente con ID: {}", patientId);
        
        try {
            patientService.updateEmergencyContact(patientId, emergencyContactName, emergencyContactPhone);
            return ResponseEntity.ok(new MessageResponse("Contacto de emergencia actualizado exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Paciente no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (PatientValidationException e) {
            log.warn("Error de validación al actualizar contacto de emergencia: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar contacto de emergencia del paciente {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al actualizar contacto de emergencia: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para buscar pacientes por nombre
     * Accesible por administradores y médicos
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> searchPatientsByName(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para buscar pacientes por nombre: {}", name);
        
        try {
            Page<PatientDto> patients = patientService.searchPatientsByName(name, pageable);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error al buscar pacientes por nombre: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al buscar pacientes: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para buscar pacientes con criterios avanzados
     * Accesible por administradores y médicos
     */
    @PostMapping("/advanced-search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> advancedSearch(
            @RequestBody PatientSearchCriteria criteria,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para búsqueda avanzada de pacientes: {}", criteria);
        
        try {
            Page<PatientDto> patients = patientService.searchPatients(criteria, pageable);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error en búsqueda avanzada de pacientes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al buscar pacientes: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener pacientes por género
     * Accesible por administradores y médicos
     */
    @GetMapping("/by-gender/{gender}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getPatientsByGender(
            @PathVariable Gender gender,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para obtener pacientes por género: {}", gender);
        
        try {
            Page<PatientDto> patients = patientService.findPatientsByGender(gender, pageable);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error al obtener pacientes por género: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener pacientes: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener pacientes por rango de edad
     * Accesible por administradores y médicos
     */
    @GetMapping("/by-age-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getPatientsByAgeRange(
            @RequestParam int minAge,
            @RequestParam int maxAge,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para obtener pacientes por rango de edad: {} - {}", minAge, maxAge);
        
        try {
            Page<PatientDto> patients = patientService.findPatientsByAgeRange(minAge, maxAge, pageable);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error al obtener pacientes por rango de edad: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener pacientes: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener pacientes por proveedor de seguro
     * Accesible por administradores y médicos
     */
    @GetMapping("/by-insurance/{provider}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getPatientsByInsurance(
            @PathVariable String provider,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para obtener pacientes por proveedor de seguro: {}", provider);
        
        try {
            Page<PatientDto> patients = patientService.findPatientsByInsuranceProvider(provider, pageable);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error al obtener pacientes por proveedor de seguro: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener pacientes: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener pacientes de un médico específico
     * Accesible por administradores y el médico correspondiente
     */
    @GetMapping("/by-doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentDoctor(#doctorId)")
    public ResponseEntity<?> getPatientsByDoctor(
            @PathVariable Long doctorId,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para obtener pacientes del doctor con ID: {}", doctorId);
        
        try {
            Page<PatientDto> patients = patientService.findPatientsByDoctor(doctorId, pageable);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error al obtener pacientes del doctor {}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener pacientes: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener pacientes sin citas recientes
     * Accesible solo por administradores
     */
    @GetMapping("/without-recent-appointments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPatientsWithoutRecentAppointments(
            @RequestParam(defaultValue = "6 MONTH") String interval,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para obtener pacientes sin citas recientes (intervalo: {})", interval);
        
        try {
            Page<PatientDto> patients = patientService.findPatientsWithoutRecentAppointments(interval, pageable);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error al obtener pacientes sin citas recientes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener pacientes: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener pacientes nuevos
     * Accesible solo por administradores
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getNewPatients(
            @RequestParam(defaultValue = "1 MONTH") String interval,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        
        log.info("Solicitud para obtener pacientes nuevos (intervalo: {})", interval);
        
        try {
            Page<PatientDto> patients = patientService.findNewPatients(interval, pageable);
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            log.error("Error al obtener pacientes nuevos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener pacientes: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener estadísticas de pacientes por género
     * Accesible solo por administradores
     */
    @GetMapping("/stats/by-gender")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPatientStatsByGender() {
        
        log.info("Solicitud para obtener estadísticas de pacientes por género");
        
        try {
            List<Object[]> stats = patientService.getPatientStatsByGender();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas por género: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para obtener estadísticas de pacientes por grupo de edad
     * Accesible solo por administradores
     */
    @GetMapping("/stats/by-age-group")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPatientStatsByAgeGroup(
            @RequestParam(defaultValue = "10") int interval) {
        
        log.info("Solicitud para obtener estadísticas de pacientes por grupo de edad (intervalo: {})", interval);
        
        try {
            List<Object[]> stats = patientService.getPatientStatsByAgeGroup(interval);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas por grupo de edad: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para fusionar registros de pacientes duplicados
     * Accesible solo por administradores
     */
    @PostMapping("/merge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> mergePatientRecords(
            @RequestParam Long keepPatientId,
            @RequestParam Long mergePatientId,
            @RequestParam String reason) {
        
        log.info("Solicitud para fusionar paciente {} en paciente {} con razón: {}", 
                mergePatientId, keepPatientId, reason);
        
        try {
            patientService.mergePatientRecords(keepPatientId, mergePatientId, reason);
            return ResponseEntity.ok(new MessageResponse("Pacientes fusionados exitosamente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Paciente no encontrado: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (PatientValidationException e) {
            log.warn("Error de validación al fusionar pacientes: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al fusionar pacientes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Error al fusionar pacientes: " + e.getMessage()));
        }
    }
}