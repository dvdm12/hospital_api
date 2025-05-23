package com.example.miapp.controller;

import com.example.miapp.dto.PatientRoomDto;
import com.example.miapp.services.PatientRoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing patient-room assignments.
 */
@RestController
@RequestMapping("/api/patient-rooms")
@RequiredArgsConstructor
public class PatientRoomController {

    private final PatientRoomService patientRoomService;

    /**
     * Retrieves all patient-room assignments.
     *
     * @return List of {@link PatientRoomDto} containing all assignments.
     */
    @GetMapping
    public ResponseEntity<List<PatientRoomDto>> getAllPatientRooms() {
        return ResponseEntity.ok(patientRoomService.getAllPatientRooms());
    }

    /**
     * Retrieves a patient-room assignment by its ID.
     *
     * @param id The ID of the patient-room assignment.
     * @return {@link PatientRoomDto} containing the requested assignment details.
     * @throws EntityNotFoundException If the assignment does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientRoomDto> getPatientRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(patientRoomService.getPatientRoomById(id));
    }

    /**
     * Creates a new patient-room assignment.
     *
     * @param patientRoomDto The data of the new assignment.
     * @return The created {@link PatientRoomDto}.
     */
    @PostMapping
    public ResponseEntity<PatientRoomDto> createPatientRoom(@Valid @RequestBody PatientRoomDto patientRoomDto) {
        return ResponseEntity.ok(patientRoomService.savePatientRoom(patientRoomDto));
    }

    /**
     * Updates an existing patient-room assignment.
     *
     * @param id             The ID of the assignment to update.
     * @param patientRoomDto The new data for the assignment.
     * @return The updated {@link PatientRoomDto}.
     * @throws EntityNotFoundException If the assignment does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientRoomDto> updatePatientRoom(@PathVariable Long id, @Valid @RequestBody PatientRoomDto patientRoomDto) {
        return ResponseEntity.ok(patientRoomService.updatePatientRoom(id, patientRoomDto));
    }

    /**
     * Deletes a patient-room assignment by its ID.
     *
     * @param id The ID of the assignment to delete.
     * @return Response with status 204 (No Content) if deletion is successful.
     * @throws EntityNotFoundException If the assignment does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatientRoom(@PathVariable Long id) {
        patientRoomService.deletePatientRoom(id);
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

    /**
     * Handles exceptions related to invalid arguments (e.g., check-out before check-in).
     *
     * @param ex The exception thrown.
     * @return ResponseEntity with status 400 (Bad Request) and error message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(ex.getMessage());
    }
}
