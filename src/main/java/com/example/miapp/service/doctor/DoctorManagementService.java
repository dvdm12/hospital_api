package com.example.miapp.service.doctor;

import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSchedule;
import com.example.miapp.entity.DoctorSpecialty;
import com.example.miapp.entity.Specialty;
import com.example.miapp.repository.DoctorRepository;
import com.example.miapp.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Service responsible for doctor entity management operations (Single Responsibility)
 * Implements Command Pattern for operations
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DoctorManagementService {

    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;

    /**
     * Updates doctor consultation fee
     */
    public void updateConsultationFee(Long doctorId, Double consultationFee) {
        log.info("Updating consultation fee for doctor {} to {}", doctorId, consultationFee);
        
        int updatedRows = doctorRepository.updateConsultationFee(doctorId, consultationFee);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update consultation fee for doctor: " + doctorId);
        }
        
        log.info("Successfully updated consultation fee for doctor {}", doctorId);
    }

    /**
     * Updates doctor biography
     */
    public void updateBiography(Long doctorId, String biography) {
        log.info("Updating biography for doctor {}", doctorId);
        
        int updatedRows = doctorRepository.updateBiography(doctorId, biography);
        
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update biography for doctor: " + doctorId);
        }
        
        log.info("Successfully updated biography for doctor {}", doctorId);
    }

    /**
     * Adds specialty to doctor
     */
    public void addSpecialtyToDoctor(Long doctorId, Long specialtyId, String experienceLevel, Date certificationDate) {
        log.info("Adding specialty {} to doctor {} with experience level {}", specialtyId, doctorId, experienceLevel);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
        
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new RuntimeException("Specialty not found: " + specialtyId));
        
        // Check if doctor already has this specialty
        boolean hasSpecialty = doctor.getDoctorSpecialties().stream()
                .anyMatch(ds -> ds.getSpecialty().getId().equals(specialtyId));
        
        if (hasSpecialty) {
            throw new RuntimeException("Doctor already has specialty: " + specialty.getName());
        }
        
        DoctorSpecialty doctorSpecialty = DoctorSpecialty.builder()
                .doctor(doctor)
                .specialty(specialty)
                .experienceLevel(experienceLevel)
                .certificationDate(certificationDate)
                .build();
        
        doctor.getDoctorSpecialties().add(doctorSpecialty);
        doctorRepository.save(doctor);
        
        log.info("Successfully added specialty {} to doctor {}", specialtyId, doctorId);
    }

    /**
     * Removes specialty from doctor
     */
    public void removeSpecialtyFromDoctor(Long doctorId, Long specialtyId) {
        log.info("Removing specialty {} from doctor {}", specialtyId, doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
        
        // Ensure doctor has at least one specialty after removal
        if (doctor.getDoctorSpecialties().size() <= 1) {
            throw new RuntimeException("Doctor must have at least one specialty");
        }
        
        boolean removed = doctor.getDoctorSpecialties().removeIf(
                ds -> ds.getSpecialty().getId().equals(specialtyId));
        
        if (!removed) {
            throw new RuntimeException("Doctor does not have specialty: " + specialtyId);
        }
        
        doctorRepository.save(doctor);
        
        log.info("Successfully removed specialty {} from doctor {}", specialtyId, doctorId);
    }

    /**
     * Adds work schedule to doctor
     */
    public void addWorkSchedule(Long doctorId, DoctorSchedule schedule) {
        log.info("Adding work schedule for doctor {} on {}", doctorId, schedule.getDayOfWeek());
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
        
        // Check if doctor already has schedule for this day
        boolean hasScheduleForDay = doctor.getWorkSchedules().stream()
                .anyMatch(ws -> ws.getDayOfWeek() == schedule.getDayOfWeek() && ws.isActive());
        
        if (hasScheduleForDay) {
            throw new RuntimeException("Doctor already has active schedule for " + schedule.getDayOfWeek());
        }
        
        schedule.setDoctor(doctor);
        doctor.getWorkSchedules().add(schedule);
        doctorRepository.save(doctor);
        
        log.info("Successfully added work schedule for doctor {} on {}", doctorId, schedule.getDayOfWeek());
    }

    /**
     * Updates work schedule for doctor
     */
    public void updateWorkSchedule(Long doctorId, DoctorSchedule updatedSchedule) {
        log.info("Updating work schedule {} for doctor {}", updatedSchedule.getId(), doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
        
        DoctorSchedule existingSchedule = doctor.getWorkSchedules().stream()
                .filter(ws -> ws.getId().equals(updatedSchedule.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Schedule not found: " + updatedSchedule.getId()));
        
        // Update schedule fields
        existingSchedule.setStartTime(updatedSchedule.getStartTime());
        existingSchedule.setEndTime(updatedSchedule.getEndTime());
        existingSchedule.setSlotDurationMinutes(updatedSchedule.getSlotDurationMinutes());
        existingSchedule.setLocation(updatedSchedule.getLocation());
        existingSchedule.setActive(updatedSchedule.isActive());
        
        doctorRepository.save(doctor);
        
        log.info("Successfully updated work schedule {} for doctor {}", updatedSchedule.getId(), doctorId);
    }

    /**
     * Removes work schedule from doctor
     */
    public void removeWorkSchedule(Long doctorId, Long scheduleId) {
        log.info("Removing work schedule {} from doctor {}", scheduleId, doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
        
        boolean removed = doctor.getWorkSchedules().removeIf(ws -> ws.getId().equals(scheduleId));
        
        if (!removed) {
            throw new RuntimeException("Schedule not found: " + scheduleId);
        }
        
        doctorRepository.save(doctor);
        
        log.info("Successfully removed work schedule {} from doctor {}", scheduleId, doctorId);
    }

    /**
     * Activates/deactivates doctor schedule
     */
    public void toggleScheduleStatus(Long doctorId, Long scheduleId, boolean active) {
        log.info("Setting schedule {} status to {} for doctor {}", scheduleId, active, doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
        
        DoctorSchedule schedule = doctor.getWorkSchedules().stream()
                .filter(ws -> ws.getId().equals(scheduleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Schedule not found: " + scheduleId));
        
        schedule.setActive(active);
        doctorRepository.save(doctor);
        
        log.info("Successfully {} schedule {} for doctor {}", 
                active ? "activated" : "deactivated", scheduleId, doctorId);
    }

    /**
     * Updates doctor profile information
     */
    public void updateDoctorProfile(Long doctorId, String firstName, String lastName, 
                                  String phone, String profilePicture) {
        log.info("Updating profile for doctor {}", doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
        
        if (firstName != null) doctor.setFirstName(firstName);
        if (lastName != null) doctor.setLastName(lastName);
        if (phone != null) doctor.setPhone(phone);
        if (profilePicture != null) doctor.setProfilePicture(profilePicture);
        
        doctorRepository.save(doctor);
        
        log.info("Successfully updated profile for doctor {}", doctorId);
    }

    /**
     * Deletes doctor (soft delete would be better)
     */
    public void deleteDoctor(Long doctorId) {
        log.info("Deleting doctor {}", doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));
        
        // Additional validations should be performed by validation service
        doctorRepository.delete(doctor);
        
        log.info("Successfully deleted doctor {}", doctorId);
    }
}