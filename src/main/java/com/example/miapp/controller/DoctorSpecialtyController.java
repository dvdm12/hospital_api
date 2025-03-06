package com.example.miapp.controller;

import com.example.miapp.dto.DoctorSpecialtyDto;
import com.example.miapp.services.DoctorSpecialtyService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing doctor-specialty assignments.
 */
@RestController
@RequestMapping("/api/doctor-specialties")
@RequiredArgsConstructor
public class DoctorSpecialtyController {

    private final DoctorSpecialtyService doctorSpecialtyService;

    /**
     * Retrieves all doctor-specialty assignments.
     *
     * @return List of {@link DoctorSpecialtyDto} containing all assignments.
     */
    @GetMapping
    public ResponseEntity<List<DoctorSpecialtyDto>> getAllDoctorSpecialties() {
        return ResponseEntity.ok(doctorSpecialtyService.getAllDoctorSpecialties());
    }

    /**
     * Retrieves a doctor-specialty assignment by its ID.
     *
     * @param id The ID of the assignment.
     * @return {@link DoctorSpecialtyDto} containing the requested assignment details.
     * @throws EntityNotFoundException If the assignment does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorSpecialtyDto> getDoctorSpecialtyById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorSpecialtyService.getDoctorSpecialtyById(id));
    }

    /**
     * Creates a new doctor-specialty assignment.
     *
     * @param doctorSpecialtyDto The data of the new assignment.
     * @return The created {@link DoctorSpecialtyDto}.
     */
    @PostMapping
    public ResponseEntity<DoctorSpecialtyDto> createDoctorSpecialty(@Valid @RequestBody DoctorSpecialtyDto doctorSpecialtyDto) {
        return ResponseEntity.ok(doctorSpecialtyService.saveDoctorSpecialty(doctorSpecialtyDto));
    }

    /**
     * Updates an existing doctor-specialty assignment.
     *
     * @param id                  The ID of the assignment to update.
     * @param doctorSpecialtyDto   The new data for the assignment.
     * @return The updated {@link DoctorSpecialtyDto}.
     * @throws EntityNotFoundException If the assignment does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorSpecialtyDto> updateDoctorSpecialty(@PathVariable Long id, @Valid @RequestBody DoctorSpecialtyDto doctorSpecialtyDto) {
        return ResponseEntity.ok(doctorSpecialtyService.updateDoctorSpecialty(id, doctorSpecialtyDto));
    }

    /**
     * Deletes a doctor-specialty assignment by its ID.
     *
     * @param id The ID of the assignment to delete.
     * @return Response with status 204 (No Content) if deletion is successful.
     * @throws EntityNotFoundException If the assignment does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctorSpecialty(@PathVariable Long id) {
        doctorSpecialtyService.deleteDoctorSpecialty(id);
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
