package com.example.miapp.controller;

import com.example.miapp.dto.AppointmentDto;
import com.example.miapp.services.AppointmentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing appointment-related operations.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Retrieves all appointments.
     *
     * @return List of {@link AppointmentDto} containing all appointments.
     */
    @GetMapping
    public ResponseEntity<List<AppointmentDto>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    /**
     * Retrieves an appointment by its ID.
     *
     * @param id The ID of the appointment.
     * @return {@link AppointmentDto} containing the requested appointment details.
     * @throws EntityNotFoundException If the appointment does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    /**
     * Creates a new appointment.
     *
     * @param appointmentDto The data of the new appointment.
     * @return The created {@link AppointmentDto}.
     */
    @PostMapping
    public ResponseEntity<AppointmentDto> createAppointment(@Valid @RequestBody AppointmentDto appointmentDto) {
        return ResponseEntity.ok(appointmentService.saveAppointment(appointmentDto));
    }

    /**
     * Updates an existing appointment.
     *
     * @param id             The ID of the appointment to update.
     * @param appointmentDto The new data for the appointment.
     * @return The updated {@link AppointmentDto}.
     * @throws EntityNotFoundException If the appointment does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDto> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentDto appointmentDto) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, appointmentDto));
    }

    /**
     * Deletes an appointment by its ID.
     *
     * @param id The ID of the appointment to delete.
     * @return Response with status 204 (No Content) if deletion is successful.
     * @throws EntityNotFoundException If the appointment does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
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
