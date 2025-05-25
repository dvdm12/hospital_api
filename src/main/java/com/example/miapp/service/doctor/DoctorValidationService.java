package com.example.miapp.service.doctor;

import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSchedule;
import com.example.miapp.exception.DoctorValidationException;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.SpecialtyRepository;
import com.example.miapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/**
 * Service responsible for doctor validations (Single Responsibility)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorValidationService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;

    /**
     * Validates doctor creation request
     */
    public void validateDoctorCreation(CreateDoctorRequest request) {
        validateUniqueEmail(request.getEmail(), null);
        validateUniqueLicenseNumber(request.getLicenseNumber(), null);
        validateUniqueUsername(request.getUsername(), null);
        validateConsultationFee(request.getConsultationFee());
        validateSpecialties(request.getSpecialtyIds());
    }

    /**
     * Validates doctor update
     */
    public void validateDoctorUpdate(Long doctorId, CreateDoctorRequest request) {
        validateUniqueEmail(request.getEmail(), doctorId);
        validateUniqueLicenseNumber(request.getLicenseNumber(), doctorId);
        validateConsultationFee(request.getConsultationFee());
        validateSpecialties(request.getSpecialtyIds());
    }

    /**
     * Validates doctor schedule
     */
    public void validateDoctorSchedule(DoctorSchedule schedule) {
        validateScheduleTime(schedule.getStartTime(), schedule.getEndTime());
        validateSlotDuration(schedule.getSlotDurationMinutes(), schedule.getStartTime(), schedule.getEndTime());
        validateDayOfWeek(schedule.getDayOfWeek());
    }

    /**
     * Validates if doctor can be deleted
     */
    public void validateDoctorDeletion(Doctor doctor) {
        // Check if doctor has active appointments
        boolean hasActiveAppointments = doctor.getAppointments() != null && 
            doctor.getAppointments().stream()
                .anyMatch(appointment -> 
                    appointment.getStatus().name().equals("SCHEDULED") || 
                    appointment.getStatus().name().equals("CONFIRMED"));

        if (hasActiveAppointments) {
            throw new DoctorValidationException("Cannot delete doctor with active appointments");
        }

        // Check if doctor has active prescriptions
        boolean hasActivePrescriptions = doctor.getPrescriptions() != null &&
            doctor.getPrescriptions().stream()
                .anyMatch(prescription -> 
                    prescription.getStatus().name().equals("ACTIVE"));

        if (hasActivePrescriptions) {
            throw new DoctorValidationException("Cannot delete doctor with active prescriptions");
        }
    }

    /**
     * Validates consultation fee update
     */
    public void validateConsultationFeeUpdate(Double newFee) {
        validateConsultationFee(newFee);
    }

    // Private validation methods

    private void validateUniqueEmail(String email, Long excludeDoctorId) {
        doctorRepository.findByEmail(email)
            .filter(doctor -> excludeDoctorId == null || !doctor.getId().equals(excludeDoctorId))
            .ifPresent(doctor -> {
                throw new DoctorValidationException("Email already exists: " + email);
            });
    }

    private void validateUniqueLicenseNumber(String licenseNumber, Long excludeDoctorId) {
        doctorRepository.findByLicenseNumber(licenseNumber)
            .filter(doctor -> excludeDoctorId == null || !doctor.getId().equals(excludeDoctorId))
            .ifPresent(doctor -> {
                throw new DoctorValidationException("License number already exists: " + licenseNumber);
            });
    }

    private void validateUniqueUsername(String username, Long excludeUserId) {
        if (userRepository.existsByUsername(username)) {
            throw new DoctorValidationException("Username already exists: " + username);
        }
    }

    private void validateConsultationFee(Double fee) {
        if (fee != null && fee < 0) {
            throw new DoctorValidationException("Consultation fee cannot be negative");
        }
        if (fee != null && fee > 10000) {
            throw new DoctorValidationException("Consultation fee seems too high: " + fee);
        }
    }

    private void validateSpecialties(Set<Long> specialtyIds) {
        if (specialtyIds == null || specialtyIds.isEmpty()) {
            throw new DoctorValidationException("Doctor must have at least one specialty");
        }

        for (Long specialtyId : specialtyIds) {
            if (!specialtyRepository.existsById(specialtyId)) {
                throw new DoctorValidationException("Specialty not found with ID: " + specialtyId);
            }
        }
    }

    private void validateScheduleTime(LocalTime startTime, LocalTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new DoctorValidationException("Start time cannot be after end time");
        }

        if (startTime.equals(endTime)) {
            throw new DoctorValidationException("Start time cannot equal end time");
        }
    }

    private void validateSlotDuration(Integer slotDurationMinutes, LocalTime startTime, LocalTime endTime) {
        if (slotDurationMinutes <= 0) {
            throw new DoctorValidationException("Slot duration must be positive");
        }

        if (slotDurationMinutes > 480) { // 8 hours
            throw new DoctorValidationException("Slot duration cannot exceed 8 hours");
        }

        // Validate that slot duration fits within the work period
        long workPeriodMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (slotDurationMinutes > workPeriodMinutes) {
            throw new DoctorValidationException(
                "Slot duration cannot be longer than work period: " + workPeriodMinutes + " minutes");
        }
    }

    private void validateDayOfWeek(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            throw new DoctorValidationException("Day of week is required for schedule");
        }
    }
}