package com.example.miapp.service.doctor;

import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.dto.doctor.DoctorDto;
import com.example.miapp.dto.doctor.DoctorScheduleDto;
import com.example.miapp.dto.doctor.DoctorSearchCriteria;
import com.example.miapp.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Interface Segregation Principle - Clean contract for doctor operations
 * Dependency Inversion Principle - High-level modules depend on abstractions
 */
public interface DoctorService {

    // CRUD Operations
    DoctorDto createDoctor(CreateDoctorRequest request);
    DoctorDto updateDoctor(Long doctorId, CreateDoctorRequest request);
    void deleteDoctor(Long doctorId);

    // Query Operations
    DoctorDto getDoctor(Long doctorId);
    Doctor findDoctorById(Long doctorId); // For internal service use
    Optional<DoctorDto> findDoctorByEmail(String email);
    Page<DoctorDto> searchDoctorsByName(String namePattern, Pageable pageable);
    Page<DoctorDto> findDoctorsBySpecialty(Long specialtyId, Pageable pageable);
    Page<DoctorDto> findAvailableDoctors(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Pageable pageable);
    Page<DoctorDto> searchDoctors(DoctorSearchCriteria criteria, Pageable pageable);
    Page<DoctorDto> getAllDoctors(Pageable pageable);

    // Management Operations
    void updateConsultationFee(Long doctorId, Double consultationFee);
    void updateBiography(Long doctorId, String biography);
    void addSpecialtyToDoctor(Long doctorId, Long specialtyId, String experienceLevel, Date certificationDate);
    void removeSpecialtyFromDoctor(Long doctorId, Long specialtyId);
    void addWorkSchedule(Long doctorId, DoctorScheduleDto schedule);
    void updateWorkSchedule(Long doctorId, DoctorScheduleDto schedule);
    void removeWorkSchedule(Long doctorId, Long scheduleId);

    // Statistics and Reporting
    List<Object[]> getAppointmentStatsByDoctor(LocalDateTime startDate, LocalDateTime endDate);
    Page<DoctorDto> findDoctorsByConsultationFeeRange(Double minFee, Double maxFee, Pageable pageable);
}