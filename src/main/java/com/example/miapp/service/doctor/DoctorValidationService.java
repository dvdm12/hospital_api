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
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/**
 * Service responsible for doctor validations (Single Responsibility)
 * Modified for improved validation and concurrency handling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorValidationService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;

    /**
     * Validación completa y robusta para la creación de doctores
     */
    @Transactional(readOnly = true)
    public void validateDoctorCreation(CreateDoctorRequest request) {
        if (request == null) {
            throw new DoctorValidationException("Request cannot be null");
        }
        
        // Validaciones de campos obligatorios
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new DoctorValidationException("Username is required");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new DoctorValidationException("Email is required");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new DoctorValidationException("Password is required");
        }
        
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new DoctorValidationException("First name is required");
        }
        
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new DoctorValidationException("Last name is required");
        }
        
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new DoctorValidationException("Phone number is required");
        }
        
        // Validaciones existentes mejoradas
        validateUniqueEmail(request.getEmail(), null);
        validateUniqueLicenseNumber(request.getLicenseNumber(), null);
        validateUniqueUsername(request.getUsername(), null);
        validateConsultationFee(request.getConsultationFee());
        validateSpecialtiesForCreation(request.getSpecialtyIds());
        validatePhoneNumber(request.getPhone());
    }

    /**
     * Validates doctor update
     */
    @Transactional(readOnly = true)
    public void validateDoctorUpdate(Long doctorId, CreateDoctorRequest request) {
        if (doctorId == null) {
            throw new DoctorValidationException("Doctor ID cannot be null");
        }
        
        if (request == null) {
            throw new DoctorValidationException("Update request cannot be null");
        }
        
        // Verificar que el doctor existe
        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorValidationException("Doctor not found with ID: " + doctorId);
        }
        
        // Validar campos que se están actualizando
        if (request.getEmail() != null) {
            validateUniqueEmail(request.getEmail(), doctorId);
        }
        
        if (request.getLicenseNumber() != null) {
            validateUniqueLicenseNumber(request.getLicenseNumber(), doctorId);
        }
        
        if (request.getConsultationFee() != null) {
            validateConsultationFee(request.getConsultationFee());
        }
        
        if (request.getPhone() != null) {
            validatePhoneNumber(request.getPhone());
        }
        
        if (request.getSpecialtyIds() != null) {
            validateSpecialtiesForUpdate(request.getSpecialtyIds());
        }
    }

    /**
     * Validates doctor schedule
     */
    public void validateDoctorSchedule(DoctorSchedule schedule) {
        if (schedule == null) {
            throw new DoctorValidationException("Schedule cannot be null");
        }
        
        validateScheduleTime(schedule.getStartTime(), schedule.getEndTime());
        validateSlotDuration(schedule.getSlotDurationMinutes(), schedule.getStartTime(), schedule.getEndTime());
        validateDayOfWeek(schedule.getDayOfWeek());
        
        if (schedule.getLocation() == null || schedule.getLocation().trim().isEmpty()) {
            throw new DoctorValidationException("Schedule location is required");
        }
    }

    /**
     * Validates if doctor can be deleted
     */
    @Transactional(readOnly = true)
    public void validateDoctorDeletion(Doctor doctor) {
        if (doctor == null) {
            throw new DoctorValidationException("Doctor cannot be null");
        }
        
        // Check if doctor has active appointments
        boolean hasActiveAppointments = doctor.getAppointments() != null && 
            doctor.getAppointments().stream()
                .anyMatch(appointment -> 
                    appointment.getStatus().name().equals("SCHEDULED") || 
                    appointment.getStatus().name().equals("CONFIRMED"));

        if (hasActiveAppointments) {
            throw new DoctorValidationException("Cannot delete doctor with active appointments. " +
                    "Please cancel or reassign all appointments first.");
        }

        // Check if doctor has active prescriptions
        boolean hasActivePrescriptions = doctor.getPrescriptions() != null &&
            doctor.getPrescriptions().stream()
                .anyMatch(prescription -> 
                    prescription.getStatus().name().equals("ACTIVE"));

        if (hasActivePrescriptions) {
            throw new DoctorValidationException("Cannot delete doctor with active prescriptions. " +
                    "Please mark all prescriptions as completed or transfer them to another doctor.");
        }
    }

    /**
     * Validates consultation fee update
     */
    public void validateConsultationFeeUpdate(Double newFee) {
        validateConsultationFee(newFee);
    }

    // Private validation methods

    /**
     * Validates that email is unique and properly formatted
     */
    private void validateUniqueEmail(String email, Long excludeDoctorId) {
        if (email == null) {
            throw new DoctorValidationException("Email cannot be null");
        }
        
        String trimmedEmail = email.trim().toLowerCase();
        if (trimmedEmail.isEmpty()) {
            throw new DoctorValidationException("Email cannot be empty");
        }
        
        // Validación básica de formato de email
        if (!trimmedEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new DoctorValidationException("Invalid email format: " + email);
        }
        
        doctorRepository.findByEmail(trimmedEmail)
            .filter(doctor -> excludeDoctorId == null || !doctor.getId().equals(excludeDoctorId))
            .ifPresent(doctor -> {
                throw new DoctorValidationException("Email already exists: " + email);
            });
    }

    /**
     * Validates that license number is unique
     */
    private void validateUniqueLicenseNumber(String licenseNumber, Long excludeDoctorId) {
        // Si el licenseNumber es nulo, generamos uno automáticamente en el servicio
        if (licenseNumber == null) {
            return;
        }
        
        // Si el licenseNumber comienza con "POR ASIGNAR-", permitirlo durante la creación
        if (licenseNumber.startsWith("POR ASIGNAR-")) {
            log.info("Permitiendo licencia temporal: {}", licenseNumber);
            return;
        }
        
        // Validar que no esté vacío
        String trimmedLicense = licenseNumber.trim();
        if (trimmedLicense.isEmpty()) {
            throw new DoctorValidationException("License number cannot be empty");
        }
        
        // Validar longitud mínima
        if (trimmedLicense.length() < 5) {
            throw new DoctorValidationException("License number is too short (minimum 5 characters)");
        }
        
        // Validar unicidad
        doctorRepository.findByLicenseNumber(trimmedLicense)
            .filter(doctor -> excludeDoctorId == null || !doctor.getId().equals(excludeDoctorId))
            .ifPresent(doctor -> {
                throw new DoctorValidationException("License number already exists: " + licenseNumber);
            });
    }

    /**
     * Validates that username is unique and properly formatted
     */
    private void validateUniqueUsername(String username, Long excludeUserId) {
        if (username == null) {
            throw new DoctorValidationException("Username cannot be null");
        }
        
        String trimmedUsername = username.trim();
        if (trimmedUsername.isEmpty()) {
            throw new DoctorValidationException("Username cannot be empty");
        }
        
        // Validar longitud mínima
        if (trimmedUsername.length() < 3) {
            throw new DoctorValidationException("Username is too short (minimum 3 characters)");
        }
        
        // Validar caracteres permitidos
        if (!trimmedUsername.matches("^[a-zA-Z0-9._-]+$")) {
            throw new DoctorValidationException("Username contains invalid characters (only letters, numbers, dots, underscores and hyphens are allowed)");
        }
        
        // Validar unicidad
        if (userRepository.existsByUsername(trimmedUsername)) {
            throw new DoctorValidationException("Username already exists: " + username);
        }
    }

    /**
     * Validates consultation fee is within acceptable range
     */
    private void validateConsultationFee(Double fee) {
        // Si es nulo, se asignará un valor por defecto en el servicio
        if (fee == null) {
            return;
        }
        
        if (fee < 0) {
            throw new DoctorValidationException("Consultation fee cannot be negative");
        }
        
        if (fee > 10000) {
            throw new DoctorValidationException("Consultation fee seems too high: " + fee + 
                    ". Please verify the amount is correct.");
        }
    }

    /**
     * Validates phone number format
     */
    private void validatePhoneNumber(String phone) {
        if (phone == null) {
            throw new DoctorValidationException("Phone number cannot be null");
        }
        
        String trimmedPhone = phone.trim();
        if (trimmedPhone.isEmpty()) {
            throw new DoctorValidationException("Phone number cannot be empty");
        }
        
        // Validar formato básico (dígitos, espacios, guiones y paréntesis)
        if (!trimmedPhone.matches("^[0-9\\s\\(\\)\\-\\+]+$")) {
            throw new DoctorValidationException("Phone number contains invalid characters: " + phone);
        }
        
        // Validar longitud mínima (sin contar caracteres no numéricos)
        String digitsOnly = trimmedPhone.replaceAll("\\D", "");
        if (digitsOnly.length() < 7) {
            throw new DoctorValidationException("Phone number is too short: " + phone);
        }
        
        if (digitsOnly.length() > 15) {
            throw new DoctorValidationException("Phone number is too long: " + phone);
        }
    }

    /**
     * Validates specialties for doctor creation - ALLOWS EMPTY SPECIALTIES DURING INITIAL CREATION
     * This is a more flexible version of validateSpecialties for initial registration
     */
    private void validateSpecialtiesForCreation(Set<Long> specialtyIds) {
        // During initial creation, we allow null or empty specialties
        // They will be assigned later or set to default in the controller
        if (specialtyIds == null || specialtyIds.isEmpty()) {
            log.warn("Creating doctor without specialties. Specialties should be assigned later.");
            return;
        }

        // If specialties are provided, validate they exist
        validateSpecialtyIds(specialtyIds);
    }
    
    /**
     * Validates specialties for doctor update - REQUIRES AT LEAST ONE SPECIALTY
     * This is the strict version used for updates after creation
     */
    private void validateSpecialtiesForUpdate(Set<Long> specialtyIds) {
        // For updates, we maintain the requirement of at least one specialty
        if (specialtyIds == null || specialtyIds.isEmpty()) {
            throw new DoctorValidationException("Doctor must have at least one specialty");
        }

        // Validate that all provided specialty IDs exist
        validateSpecialtyIds(specialtyIds);
    }
    
    /**
     * Validates that all specialty IDs exist in the database
     */
    private void validateSpecialtyIds(Set<Long> specialtyIds) {
        if (specialtyIds == null) return;
        
        for (Long specialtyId : specialtyIds) {
            if (specialtyId == null) {
                throw new DoctorValidationException("Specialty ID cannot be null");
            }
            
            if (!specialtyRepository.existsById(specialtyId)) {
                throw new DoctorValidationException("Specialty not found with ID: " + specialtyId);
            }
        }
    }

    /**
     * Validates that start time is before end time
     */
    private void validateScheduleTime(LocalTime startTime, LocalTime endTime) {
        if (startTime == null) {
            throw new DoctorValidationException("Start time cannot be null");
        }
        
        if (endTime == null) {
            throw new DoctorValidationException("End time cannot be null");
        }
        
        if (startTime.isAfter(endTime)) {
            throw new DoctorValidationException("Start time (" + startTime + ") cannot be after end time (" + endTime + ")");
        }

        if (startTime.equals(endTime)) {
            throw new DoctorValidationException("Start time cannot equal end time");
        }
        
        // Validar que no sea un horario nocturno extremo (si es política del hospital)
        // Esta validación es opcional y depende de las reglas de negocio
        if (startTime.isBefore(LocalTime.of(6, 0)) && endTime.isAfter(LocalTime.of(22, 0))) {
            log.warn("Schedule spans extreme hours (before 6 AM and after 10 PM): {} to {}", startTime, endTime);
        }
    }

    /**
     * Validates that slot duration is reasonable and fits within work period
     */
    private void validateSlotDuration(Integer slotDurationMinutes, LocalTime startTime, LocalTime endTime) {
        if (slotDurationMinutes == null) {
            throw new DoctorValidationException("Slot duration cannot be null");
        }
        
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
        
        // Validar que el periodo de trabajo sea múltiplo del slot para evitar tiempos muertos
        if (workPeriodMinutes % slotDurationMinutes != 0) {
            log.warn("Work period ({} minutes) is not a multiple of slot duration ({} minutes). " + 
                    "This may result in unused time at the end of the schedule.", 
                    workPeriodMinutes, slotDurationMinutes);
        }
    }

    /**
     * Validates day of week is provided
     */
    private void validateDayOfWeek(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            throw new DoctorValidationException("Day of week is required for schedule");
        }
    }
}