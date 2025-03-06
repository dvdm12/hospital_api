package com.example.miapp.services;

import com.example.miapp.dto.AppointmentDto;
import com.example.miapp.models.Appointment;
import com.example.miapp.models.Doctor;
import com.example.miapp.models.Patient;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing appointment-related operations.
 */
@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * Retrieves all appointments from the database.
     *
     * @return List of {@link AppointmentDto} containing appointment details.
     */
    public List<AppointmentDto> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves an appointment by its ID.
     *
     * @param id The ID of the appointment.
     * @return {@link AppointmentDto} containing appointment details.
     * @throws EntityNotFoundException If no appointment is found with the given ID.
     */
    public AppointmentDto getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with ID: " + id));
    }

    /**
     * Saves a new appointment in the database.
     *
     * @param dto The {@link AppointmentDto} containing appointment details.
     * @return The saved {@link AppointmentDto}.
     */
    @Transactional
    public AppointmentDto saveAppointment(AppointmentDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        Appointment appointment = Appointment.builder()
                .date(dto.getDate())
                .patient(patient)
                .doctor(doctor)
                .reason(dto.getReason())
                .status(dto.getStatus())
                .build();

        return convertToDto(appointmentRepository.save(appointment));
    }

    /**
     * Updates an existing appointment.
     *
     * @param id  The ID of the appointment.
     * @param dto The updated {@link AppointmentDto} data.
     * @return The updated {@link AppointmentDto}.
     * @throws EntityNotFoundException If the appointment does not exist.
     */
    @Transactional
    public AppointmentDto updateAppointment(Long id, AppointmentDto dto) {
        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with ID: " + id));

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        Appointment updatedAppointment = existingAppointment.toBuilder()
                .date(dto.getDate())
                .patient(patient)
                .doctor(doctor)
                .reason(dto.getReason())
                .status(dto.getStatus())
                .build();

        return convertToDto(appointmentRepository.save(updatedAppointment));
    }

    /**
     * Deletes an appointment by its ID.
     *
     * @param id The ID of the appointment to be deleted.
     * @throws EntityNotFoundException If the appointment does not exist.
     */
    @Transactional
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Appointment not found with ID: " + id);
        }
        appointmentRepository.deleteById(id);
    }

    /**
     * Converts an {@link Appointment} entity to an {@link AppointmentDto}.
     *
     * @param appointment The entity to convert.
     * @return The corresponding {@link AppointmentDto}.
     */
    private AppointmentDto convertToDto(Appointment appointment) {
        return AppointmentDto.builder()
                .id(appointment.getId())
                .date(appointment.getDate())
                .patientId(appointment.getPatient().getId())
                .doctorId(appointment.getDoctor().getId())
                .reason(appointment.getReason())
                .status(appointment.getStatus())
                .build();
    }
}
