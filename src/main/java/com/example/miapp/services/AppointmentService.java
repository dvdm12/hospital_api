package com.example.miapp.services;

import com.example.miapp.dto.AppointmentDto;
import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.Patient;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.repository.PatientRepository;
import com.example.miapp.repository.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing appointment-related operations.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    /**
     * Retrieves all appointments from the database.
     *
     * @return List of {@link AppointmentDto} containing appointment details.
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public AppointmentDto getAppointmentById(Long id) {
        Appointment appointment = findAppointmentById(id);
        return convertToDto(appointment);
    }

    /**
     * Saves a new appointment in the database.
     *
     * @param appointmentDto The {@link AppointmentDto} containing the new appointment details.
     * @return The saved {@link AppointmentDto}.
     * @throws EntityNotFoundException If the associated patient or doctor does not exist.
     */
    @Transactional
    public AppointmentDto saveAppointment(AppointmentDto appointmentDto) {
        Patient patient = findPatientById(appointmentDto.getPatientId());
        Doctor doctor = findDoctorById(appointmentDto.getDoctorId());

        Appointment appointment = convertToEntity(appointmentDto);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        return convertToDto(appointmentRepository.save(appointment));
    }

    /**
     * Updates an existing appointment.
     *
     * @param id             The ID of the appointment to be updated.
     * @param appointmentDto The updated {@link AppointmentDto} data.
     * @return The updated {@link AppointmentDto}.
     * @throws EntityNotFoundException If the appointment, patient, or doctor does not exist.
     */
    @Transactional
    public AppointmentDto updateAppointment(Long id, AppointmentDto appointmentDto) {
        Appointment existingAppointment = findAppointmentById(id);

        Patient patient = findPatientById(appointmentDto.getPatientId());
        Doctor doctor = findDoctorById(appointmentDto.getDoctorId());

        existingAppointment = existingAppointment.toBuilder()
                .date(appointmentDto.getDate())
                .patient(patient)
                .doctor(doctor)
                .reason(appointmentDto.getReason())
                .status(appointmentDto.getStatus())
                .build();

        return convertToDto(appointmentRepository.save(existingAppointment));
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
     * Finds an appointment by ID and throws an exception if not found.
     *
     * @param id The ID of the appointment.
     * @return The {@link Appointment} entity.
     * @throws EntityNotFoundException If the appointment does not exist.
     */
    private Appointment findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with ID: " + id));
    }

    /**
     * Finds a patient by ID and throws an exception if not found.
     *
     * @param id The ID of the patient.
     * @return The {@link Patient} entity.
     * @throws EntityNotFoundException If the patient does not exist.
     */
    private Patient findPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + id));
    }

    /**
     * Finds a doctor by ID and throws an exception if not found.
     *
     * @param id The ID of the doctor.
     * @return The {@link Doctor} entity.
     * @throws EntityNotFoundException If the doctor does not exist.
     */
    private Doctor findDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + id));
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

    /**
     * Converts an {@link AppointmentDto} to an {@link Appointment} entity.
     *
     * @param dto The DTO to convert.
     * @return The corresponding {@link Appointment} entity.
     */
    private Appointment convertToEntity(AppointmentDto dto) {
        return Appointment.builder()
                .id(dto.getId())
                .date(dto.getDate())
                .reason(dto.getReason())
                .status(dto.getStatus())
                .build();
    }
}
