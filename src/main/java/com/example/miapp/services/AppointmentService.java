package com.example.miapp.services;

import com.example.miapp.dto.AppointmentDto;
import com.example.miapp.models.Appointment;
import com.example.miapp.models.Doctor;
import com.example.miapp.models.Patient;
import com.example.miapp.repository.AppointmentRepository;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Appointment operations.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    /**
     * Retrieves all appointments.
     */
    public List<AppointmentDto> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds an appointment by ID.
     */
    public AppointmentDto getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with ID: " + id));
    }

    /**
     * Saves a new appointment.
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
     * Deletes an appointment by ID.
     */
    @Transactional
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Appointment not found with ID: " + id);
        }
        appointmentRepository.deleteById(id);
    }

    /**
     * Converts an Appointment entity to AppointmentDto.
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
