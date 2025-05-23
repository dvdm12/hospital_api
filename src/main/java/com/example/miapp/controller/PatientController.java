package com.example.miapp.controller;

import com.example.miapp.dto.PatientDto;
import com.example.miapp.services.PatientService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing patient-related operations.
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * Retrieves all patients.
     *
     * @return List of {@link PatientDto} containing all patients.
     */
    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    /**
     * Retrieves a patient by its ID.
     *
     * @param id The ID of the patient.
     * @return {@link PatientDto} containing the requested patient details.
     * @throws EntityNotFoundException If the patient does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    /**
     * Creates a new patient.
     *
     * @param patientDto The data of the new patient.
     * @return The created {@link PatientDto}.
     */
    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody PatientDto patientDto) {
        return ResponseEntity.ok(patientService.savePatient(patientDto));
    }

    /**
     * Updates an existing patient.
     *
     * @param id         The ID of the patient to update.
     * @param patientDto The new data for the patient.
     * @return The updated {@link PatientDto}.
     * @throws EntityNotFoundException If the patient does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientDto patientDto) {
        return ResponseEntity.ok(patientService.updatePatient(id, patientDto));
    }

    /**
     * Deletes a patient by its ID.
     *
     * @param id The ID of the patient to delete.
     * @return Response with status 204 (No Content) if deletion is successful.
     * @throws EntityNotFoundException If the patient does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Handles exceptions related to entity not found errors.
     *
     * @param ex The exception thrown.
     * @return ResponseEntity with status 404 (Not Found) and error message.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
