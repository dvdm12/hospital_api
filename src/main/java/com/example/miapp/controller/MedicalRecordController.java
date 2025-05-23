package com.example.miapp.controller;

import com.example.miapp.dto.MedicalRecordDto;
import com.example.miapp.services.MedicalRecordService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing medical records.
 */
@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    /**
     * Retrieves all medical records.
     *
     * @return List of {@link MedicalRecordDto} containing all medical records.
     */
    @GetMapping
    public ResponseEntity<List<MedicalRecordDto>> getAllMedicalRecords() {
        return ResponseEntity.ok(medicalRecordService.getAllMedicalRecords());
    }

    /**
     * Retrieves a medical record by its ID.
     *
     * @param id The ID of the medical record.
     * @return {@link MedicalRecordDto} containing the requested medical record.
     * @throws EntityNotFoundException If the record does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordDto> getMedicalRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.getMedicalRecordById(id));
    }

    /**
     * Creates a new medical record.
     *
     * @param medicalRecordDto The data of the new medical record.
     * @return The created {@link MedicalRecordDto}.
     */
    @PostMapping
    public ResponseEntity<MedicalRecordDto> createMedicalRecord(@Valid @RequestBody MedicalRecordDto medicalRecordDto) {
        return ResponseEntity.ok(medicalRecordService.saveMedicalRecord(medicalRecordDto));
    }

    /**
     * Updates an existing medical record.
     *
     * @param id               The ID of the medical record to update.
     * @param medicalRecordDto The new data for the medical record.
     * @return The updated {@link MedicalRecordDto}.
     * @throws EntityNotFoundException If the record does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordDto> updateMedicalRecord(@PathVariable Long id, @Valid @RequestBody MedicalRecordDto medicalRecordDto) {
        return ResponseEntity.ok(medicalRecordService.updateMedicalRecord(id, medicalRecordDto));
    }

    /**
     * Deletes a medical record by its ID.
     *
     * @param id The ID of the medical record to delete.
     * @return Response with status 204 (No Content) if deletion is successful.
     * @throws EntityNotFoundException If the record does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable Long id) {
        medicalRecordService.deleteMedicalRecord(id);
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
