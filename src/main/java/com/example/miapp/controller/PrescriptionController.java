package com.example.miapp.controller;

import com.example.miapp.dto.auth.MessageResponse;
import com.example.miapp.dto.prescription.CreatePrescriptionRequest;
import com.example.miapp.dto.prescription.PrescriptionDto;
import com.example.miapp.dto.prescription.PrescriptionItemDto;
import com.example.miapp.entity.Prescription.PrescriptionStatus;
import com.example.miapp.exception.ResourceNotFoundException;
import com.example.miapp.exception.ValidationException;
import com.example.miapp.service.prescription.PrescriptionService;
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
 * Controlador REST para la gestión de prescripciones médicas.
 * Proporciona endpoints para crear, consultar y actualizar prescripciones y medicamentos.
 */
@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Slf4j
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * Crea una nueva prescripción médica.
     * Sólo accesible por médicos.
     *
     * @param request DTO con la información de la prescripción
     * @return ResponseEntity con la prescripción creada
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> createPrescription(@Valid @RequestBody CreatePrescriptionRequest request) {
        log.info("Recibida solicitud para crear prescripción para paciente: {}", request.getPatientId());
        
        try {
            PrescriptionDto prescription = prescriptionService.createPrescription(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(prescription);
        } catch (ResourceNotFoundException e) {
            log.warn("Error al crear prescripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (ValidationException e) {
            log.warn("Error de validación al crear prescripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear prescripción: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al crear la prescripción: " + e.getMessage()));
        }
    }

    /**
     * Obtiene una prescripción por su ID.
     * Accesible por médicos y el propio paciente.
     *
     * @param prescriptionId ID de la prescripción
     * @return ResponseEntity con la información de la prescripción
     */
    @GetMapping("/{prescriptionId}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.isPatientOfPrescription(#prescriptionId)")
    public ResponseEntity<?> getPrescriptionById(@PathVariable Long prescriptionId) {
        log.info("Recibida solicitud para obtener prescripción con ID: {}", prescriptionId);
        
        try {
            PrescriptionDto prescription = prescriptionService.getPrescriptionById(prescriptionId);
            return ResponseEntity.ok(prescription);
        } catch (ResourceNotFoundException e) {
            log.warn("Prescripción no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener prescripción: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener la prescripción: " + e.getMessage()));
        }
    }

    /**
     * Obtiene todas las prescripciones para un paciente.
     * Accesible por médicos y el propio paciente.
     *
     * @param patientId ID del paciente
     * @param pageable información de paginación
     * @return ResponseEntity con la lista paginada de prescripciones
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.isCurrentUser(#patientId)")
    public ResponseEntity<?> getPrescriptionsByPatient(
            @PathVariable Long patientId,
            @PageableDefault(size = 10, sort = "issueDate") Pageable pageable) {
        log.info("Recibida solicitud para obtener prescripciones del paciente: {}", patientId);
        
        try {
            Page<PrescriptionDto> prescriptions = prescriptionService.getPrescriptionsByPatient(patientId, pageable);
            
            if (prescriptions.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "El paciente no tiene prescripciones", "content", prescriptions));
            }
            
            return ResponseEntity.ok(prescriptions);
        } catch (ResourceNotFoundException e) {
            log.warn("Error al obtener prescripciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener prescripciones del paciente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener las prescripciones: " + e.getMessage()));
        }
    }

    /**
     * Obtiene todas las prescripciones creadas por un doctor.
     * Sólo accesible por médicos.
     *
     * @param doctorId ID del doctor
     * @param pageable información de paginación
     * @return ResponseEntity con la lista paginada de prescripciones
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getPrescriptionsByDoctor(
            @PathVariable Long doctorId,
            @PageableDefault(size = 10, sort = "issueDate") Pageable pageable) {
        log.info("Recibida solicitud para obtener prescripciones del doctor: {}", doctorId);
        
        try {
            Page<PrescriptionDto> prescriptions = prescriptionService.getPrescriptionsByDoctor(doctorId, pageable);
            
            if (prescriptions.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "El doctor no ha creado prescripciones", "content", prescriptions));
            }
            
            return ResponseEntity.ok(prescriptions);
        } catch (ResourceNotFoundException e) {
            log.warn("Error al obtener prescripciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener prescripciones del doctor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener las prescripciones: " + e.getMessage()));
        }
    }

    /**
     * Obtiene la prescripción asociada a una cita.
     * Accesible por médicos y el propio paciente.
     *
     * @param appointmentId ID de la cita
     * @return ResponseEntity con la información de la prescripción
     */
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.isPatientOfAppointment(#appointmentId)")
    public ResponseEntity<?> getPrescriptionByAppointment(@PathVariable Long appointmentId) {
        log.info("Recibida solicitud para obtener prescripción de la cita: {}", appointmentId);
        
        try {
            PrescriptionDto prescription = prescriptionService.getPrescriptionByAppointment(appointmentId);
            
            if (prescription == null) {
                return ResponseEntity.ok(new MessageResponse("No hay prescripciones asociadas a esta cita"));
            }
            
            return ResponseEntity.ok(prescription);
        } catch (ResourceNotFoundException e) {
            log.warn("Error al obtener prescripción: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al obtener prescripción de la cita: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener la prescripción: " + e.getMessage()));
        }
    }

    /**
     * Actualiza el estado de una prescripción.
     * Sólo accesible por médicos.
     *
     * @param prescriptionId ID de la prescripción
     * @param statusInfo mapa con el nuevo estado
     * @return ResponseEntity con la prescripción actualizada
     */
    @PatchMapping("/{prescriptionId}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updatePrescriptionStatus(
            @PathVariable Long prescriptionId,
            @RequestBody Map<String, String> statusInfo) {
        log.info("Recibida solicitud para actualizar estado de prescripción: {}", prescriptionId);
        
        if (!statusInfo.containsKey("status")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("El campo 'status' es requerido"));
        }
        
        try {
            PrescriptionStatus status = PrescriptionStatus.valueOf(statusInfo.get("status"));
            PrescriptionDto updatedPrescription = prescriptionService.updatePrescriptionStatus(prescriptionId, status);
            
            return ResponseEntity.ok(updatedPrescription);
        } catch (IllegalArgumentException e) {
            log.warn("Estado de prescripción inválido: {}", statusInfo.get("status"));
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Estado de prescripción inválido. Valores permitidos: ACTIVE, COMPLETED, CANCELED"));
        } catch (ResourceNotFoundException e) {
            log.warn("Prescripción no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar estado de prescripción: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al actualizar el estado: " + e.getMessage()));
        }
    }

    /**
     * Marca una prescripción como impresa.
     * Sólo accesible por médicos.
     *
     * @param prescriptionId ID de la prescripción
     * @return ResponseEntity con la prescripción actualizada
     */
    @PostMapping("/{prescriptionId}/print")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> markAsPrinted(@PathVariable Long prescriptionId) {
        log.info("Recibida solicitud para marcar prescripción como impresa: {}", prescriptionId);
        
        try {
            PrescriptionDto updatedPrescription = prescriptionService.markPrescriptionAsPrinted(prescriptionId);
            
            return ResponseEntity.ok(updatedPrescription);
        } catch (ResourceNotFoundException e) {
            log.warn("Prescripción no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al marcar prescripción como impresa: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al marcar como impresa: " + e.getMessage()));
        }
    }

    /**
     * Procesa una recarga de medicamento.
     * Sólo accesible por médicos.
     *
     * @param prescriptionId ID de la prescripción
     * @param itemId ID del medicamento
     * @return ResponseEntity con la información del medicamento actualizado
     */
    @PostMapping("/{prescriptionId}/items/{itemId}/refill")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> processRefill(
            @PathVariable Long prescriptionId,
            @PathVariable Long itemId) {
        log.info("Recibida solicitud para procesar recarga de medicamento {} en prescripción: {}", 
                itemId, prescriptionId);
        
        try {
            PrescriptionItemDto updatedItem = prescriptionService.processRefill(prescriptionId, itemId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Recarga procesada exitosamente",
                "item", updatedItem
            ));
        } catch (ResourceNotFoundException e) {
            log.warn("Error al procesar recarga: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (ValidationException e) {
            log.warn("Error de validación al procesar recarga: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al procesar recarga: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al procesar la recarga: " + e.getMessage()));
        }
    }

    /**
     * Busca prescripciones por nombre de medicamento.
     * Sólo accesible por médicos.
     *
     * @param medicationName nombre del medicamento a buscar
     * @param pageable información de paginación
     * @return ResponseEntity con la lista paginada de prescripciones
     */
    @GetMapping("/search/medication")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> searchByMedication(
            @RequestParam String medicationName,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Recibida solicitud para buscar prescripciones con medicamento: {}", medicationName);
        
        try {
            Page<PrescriptionDto> prescriptions = prescriptionService.findByMedicationName(medicationName, pageable);
            
            if (prescriptions.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "message", "No se encontraron prescripciones con el medicamento especificado", 
                    "content", prescriptions
                ));
            }
            
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            log.error("Error al buscar prescripciones por medicamento: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al buscar prescripciones: " + e.getMessage()));
        }
    }

    /**
     * Busca prescripciones por diagnóstico.
     * Sólo accesible por médicos.
     *
     * @param diagnosis texto a buscar en el diagnóstico
     * @param pageable información de paginación
     * @return ResponseEntity con la lista paginada de prescripciones
     */
    @GetMapping("/search/diagnosis")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> searchByDiagnosis(
            @RequestParam String diagnosis,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Recibida solicitud para buscar prescripciones con diagnóstico: {}", diagnosis);
        
        try {
            Page<PrescriptionDto> prescriptions = prescriptionService.findByDiagnosis(diagnosis, pageable);
            
            if (prescriptions.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "message", "No se encontraron prescripciones con el diagnóstico especificado", 
                    "content", prescriptions
                ));
            }
            
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            log.error("Error al buscar prescripciones por diagnóstico: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al buscar prescripciones: " + e.getMessage()));
        }
    }

    /**
     * Verifica posibles interacciones medicamentosas para un paciente.
     * Sólo accesible por médicos.
     *
     * @param patientId ID del paciente
     * @param newMedication opcional, nuevo medicamento a verificar
     * @return ResponseEntity con la lista de posibles interacciones
     */
    @GetMapping("/interactions/{patientId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> checkDrugInteractions(
            @PathVariable Long patientId,
            @RequestParam(required = false) String newMedication) {
        log.info("Recibida solicitud para verificar interacciones medicamentosas para paciente: {}", patientId);
        
        try {
            List<String> interactions = prescriptionService.checkDrugInteractions(patientId, newMedication);
            
            Map<String, Object> response = new HashMap<>();
            
            if (interactions.isEmpty()) {
                response.put("message", "No se encontraron interacciones medicamentosas");
                response.put("hasInteractions", false);
            } else {
                response.put("message", "Se encontraron posibles interacciones medicamentosas");
                response.put("hasInteractions", true);
                response.put("interactionCount", interactions.size());
            }
            
            response.put("interactions", interactions);
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.warn("Error al verificar interacciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error al verificar interacciones medicamentosas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al verificar interacciones: " + e.getMessage()));
        }
    }

    /**
     * Obtiene los medicamentos más prescritos.
     * Sólo accesible por administradores.
     *
     * @param limit número máximo de resultados
     * @return ResponseEntity con la lista de medicamentos más prescritos
     */
    @GetMapping("/statistics/most-prescribed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMostPrescribedMedications(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Recibida solicitud para obtener los {} medicamentos más prescritos", limit);
        
        try {
            List<Object[]> statistics = prescriptionService.getMostPrescribedMedications(limit);
            
            List<Map<String, Object>> formattedStats = statistics.stream()
                    .map(stat -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("medication", stat[0]);
                        item.put("count", stat[1]);
                        return item;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(formattedStats);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de medicamentos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Busca prescripciones que necesitan renovación pronto.
     * Sólo accesible por médicos.
     *
     * @param daysThreshold número de días para considerar "pronto"
     * @param pageable información de paginación
     * @return ResponseEntity con la lista paginada de prescripciones
     */
    @GetMapping("/renewal-needed")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> findPrescriptionsNeedingRenewal(
            @RequestParam(defaultValue = "7") int daysThreshold,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("Recibida solicitud para buscar prescripciones que necesitan renovación en {} días", daysThreshold);
        
        try {
            Page<PrescriptionDto> prescriptions = prescriptionService.findPrescriptionsNeedingRenewal(daysThreshold, pageable);
            
            if (prescriptions.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "message", "No hay prescripciones que necesiten renovación próximamente", 
                    "content", prescriptions
                ));
            }
            
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            log.error("Error al buscar prescripciones para renovación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al buscar prescripciones: " + e.getMessage()));
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