package com.example.miapp.controller;

import com.example.miapp.dto.auth.MessageResponse;
import com.example.miapp.dto.medicalrecord.CreateMedicalEntryRequest;
import com.example.miapp.dto.medicalrecord.MedicalRecordDto;
import com.example.miapp.dto.medicalrecord.MedicalRecordEntryDto;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.service.medicalrecord.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de historiales médicos.
 * Proporciona endpoints para consultar y actualizar historiales médicos.
 */
@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    /**
     * Obtiene el historial médico de un paciente.
     * Accesible por médicos y el propio paciente.
     *
     * @param patientId ID del paciente
     * @return ResponseEntity con el historial médico
     */
    @GetMapping("/patients/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.isCurrentUser(#patientId)")
    public ResponseEntity<?> getMedicalRecord(@PathVariable Long patientId) {
        log.info("Recibida solicitud para obtener historial médico del paciente: {}", patientId);
        
        try {
            MedicalRecordDto medicalRecord = medicalRecordService.getMedicalRecordByPatientId(patientId);
            return ResponseEntity.ok(medicalRecord);
        } catch (ResourceNotFoundException e) {
            log.warn("Historial médico no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener historial médico: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener el historial médico: " + e.getMessage()));
        }
    }

    /**
     * Obtiene las entradas del historial médico de un paciente.
     * Accesible por médicos y el propio paciente.
     *
     * @param patientId ID del paciente
     * @return ResponseEntity con la lista de entradas
     */
    @GetMapping("/patients/{patientId}/entries")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.isCurrentUser(#patientId)")
    public ResponseEntity<?> getMedicalRecordEntries(@PathVariable Long patientId) {
        log.info("Recibida solicitud para obtener entradas del historial médico del paciente: {}", patientId);
        
        try {
            List<MedicalRecordEntryDto> entries = medicalRecordService.getMedicalRecordEntries(patientId);
            return ResponseEntity.ok(entries);
        } catch (ResourceNotFoundException e) {
            log.warn("Historial médico no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener entradas del historial médico: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener las entradas del historial médico: " + e.getMessage()));
        }
    }

    /**
     * Añade una nueva entrada al historial médico de un paciente.
     * Solo accesible por médicos.
     *
     * @param patientId ID del paciente
     * @param request DTO con la información de la nueva entrada
     * @return ResponseEntity con la entrada creada
     */
    @PostMapping("/patients/{patientId}/entries")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> addMedicalRecordEntry(
            @PathVariable Long patientId,
            @Valid @RequestBody CreateMedicalEntryRequest request) {
        log.info("Recibida solicitud para añadir entrada al historial médico del paciente: {}", patientId);
        
        try {
            MedicalRecordEntryDto entry = medicalRecordService.addMedicalRecordEntry(patientId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(entry);
        } catch (ResourceNotFoundException e) {
            log.warn("Error al añadir entrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al añadir entrada al historial médico: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al añadir entrada al historial médico: " + e.getMessage()));
        }
    }

    /**
     * Actualiza la información básica del historial médico.
     * Solo accesible por médicos.
     *
     * @param recordId ID del historial médico
     * @param updateInfo Mapa con los campos a actualizar
     * @return ResponseEntity con el historial médico actualizado
     */
    @PatchMapping("/{recordId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateMedicalRecord(
            @PathVariable Long recordId,
            @RequestBody Map<String, String> updateInfo) {
        log.info("Recibida solicitud para actualizar historial médico con ID: {}", recordId);
        
        try {
            MedicalRecordDto updatedRecord = medicalRecordService.updateMedicalRecordInfo(
                    recordId,
                    updateInfo.get("allergies"),
                    updateInfo.get("chronicConditions"),
                    updateInfo.get("currentMedications"),
                    updateInfo.get("familyHistory"),
                    updateInfo.get("surgicalHistory"),
                    updateInfo.get("notes")
            );
            
            return ResponseEntity.ok(updatedRecord);
        } catch (ResourceNotFoundException e) {
            log.warn("Historial médico no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar historial médico: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al actualizar el historial médico: " + e.getMessage()));
        }
    }

    /**
     * Actualiza la visibilidad de una entrada para el paciente.
     * Solo accesible por médicos.
     *
     * @param entryId ID de la entrada
     * @param visibilityInfo Mapa con la información de visibilidad
     * @return ResponseEntity con mensaje de éxito
     */
    @PatchMapping("/entries/{entryId}/visibility")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateEntryVisibility(
            @PathVariable Long entryId,
            @RequestBody Map<String, Boolean> visibilityInfo) {
        log.info("Recibida solicitud para actualizar visibilidad de entrada con ID: {}", entryId);
        
        if (!visibilityInfo.containsKey("visibleToPatient")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("El campo 'visibleToPatient' es requerido"));
        }
        
        boolean visibleToPatient = visibilityInfo.get("visibleToPatient");
        
        try {
            medicalRecordService.updateEntryVisibility(entryId, visibleToPatient);
            
            return ResponseEntity.ok(new MessageResponse(
                    "Visibilidad de la entrada actualizada correctamente a: " + 
                    (visibleToPatient ? "visible" : "no visible") + " para el paciente"));
        } catch (ResourceNotFoundException e) {
            log.warn("Entrada no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar visibilidad de entrada: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al actualizar la visibilidad: " + e.getMessage()));
        }
    }

    /**
     * Busca historiales médicos por condición crónica.
     * Solo accesible por médicos.
     *
     * @param condition Condición a buscar
     * @param pageable Información de paginación
     * @return ResponseEntity con los historiales encontrados
     */
    @GetMapping("/search/condition")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> searchByChronicCondition(
            @RequestParam String condition,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Recibida solicitud para buscar historiales por condición: {}", condition);
        
        try {
            Page<MedicalRecordDto> records = medicalRecordService.findByChronicCondition(condition, pageable);
            
            if (records.isEmpty()) {
                return ResponseEntity.ok(new MessageResponse("No se encontraron historiales médicos con esa condición"));
            }
            
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Error al buscar historiales por condición: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al buscar historiales: " + e.getMessage()));
        }
    }

    /**
     * Busca historiales médicos por alergia.
     * Solo accesible por médicos.
     *
     * @param allergy Alergia a buscar
     * @param pageable Información de paginación
     * @return ResponseEntity con los historiales encontrados
     */
    @GetMapping("/search/allergy")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> searchByAllergy(
            @RequestParam String allergy,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Recibida solicitud para buscar historiales por alergia: {}", allergy);
        
        try {
            Page<MedicalRecordDto> records = medicalRecordService.findByAllergy(allergy, pageable);
            
            if (records.isEmpty()) {
                return ResponseEntity.ok(new MessageResponse("No se encontraron historiales médicos con esa alergia"));
            }
            
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Error al buscar historiales por alergia: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al buscar historiales: " + e.getMessage()));
        }
    }

    /**
     * Busca historiales médicos por medicación actual.
     * Solo accesible por médicos.
     *
     * @param medication Medicación a buscar
     * @param pageable Información de paginación
     * @return ResponseEntity con los historiales encontrados
     */
    @GetMapping("/search/medication")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> searchByMedication(
            @RequestParam String medication,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Recibida solicitud para buscar historiales por medicación: {}", medication);
        
        try {
            Page<MedicalRecordDto> records = medicalRecordService.findByCurrentMedication(medication, pageable);
            
            if (records.isEmpty()) {
                return ResponseEntity.ok(new MessageResponse("No se encontraron historiales médicos con esa medicación"));
            }
            
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Error al buscar historiales por medicación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al buscar historiales: " + e.getMessage()));
        }
    }

    /**
     * Obtiene historiales médicos actualizados recientemente.
     * Solo accesible por administradores.
     *
     * @param days Número de días hacia atrás (opcional, default 7)
     * @param pageable Información de paginación
     * @return ResponseEntity con los historiales encontrados
     */
    @GetMapping("/recent-updates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRecentlyUpdated(
            @RequestParam(defaultValue = "7") int days,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Recibida solicitud para obtener historiales actualizados en los últimos {} días", days);
        
        try {
            Page<MedicalRecordDto> records = medicalRecordService.getRecentlyUpdatedRecords(days, pageable);
            
            if (records.isEmpty()) {
                return ResponseEntity.ok(new MessageResponse(
                        "No se encontraron historiales médicos actualizados en los últimos " + days + " días"));
            }
            
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Error al obtener historiales actualizados recientemente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener historiales: " + e.getMessage()));
        }
    }

    /**
     * Obtiene un resumen del historial médico para un paciente.
     * Accesible por médicos y el propio paciente.
     *
     * @param patientId ID del paciente
     * @return ResponseEntity con el resumen del historial
     */
    @GetMapping("/patients/{patientId}/summary")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.isCurrentUser(#patientId)")
    public ResponseEntity<?> getMedicalRecordSummary(@PathVariable Long patientId) {
        log.info("Recibida solicitud para obtener resumen de historial médico del paciente: {}", patientId);
        
        try {
            MedicalRecordDto summary = medicalRecordService.getMedicalRecordSummary(patientId);
            return ResponseEntity.ok(summary);
        } catch (ResourceNotFoundException e) {
            log.warn("Historial médico no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener resumen de historial médico: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener el resumen del historial médico: " + e.getMessage()));
        }
    }

    /**
     * Maneja errores de validación.
     * 
     * @param ex excepción de validación
     * @return ResponseEntity con mensajes de error
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        log.warn("Errores de validación en la solicitud: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }
}