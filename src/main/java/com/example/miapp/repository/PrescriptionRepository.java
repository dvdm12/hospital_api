package com.example.miapp.repository;

import com.example.miapp.entity.Prescription;
import com.example.miapp.entity.Prescription.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Prescription entity.
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // Basic finders
    Page<Prescription> findByPatientId(Long patientId, Pageable pageable);
    Page<Prescription> findByDoctorId(Long doctorId, Pageable pageable);
    Page<Prescription> findByStatus(PrescriptionStatus status, Pageable pageable);
    Page<Prescription> findByPatientIdAndStatus(Long patientId, PrescriptionStatus status, Pageable pageable);
    Page<Prescription> findByDoctorIdAndStatus(Long doctorId, PrescriptionStatus status, Pageable pageable);
    
    // Date range queries
    Page<Prescription> findByIssueDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Medication queries
    @Query("SELECT p FROM Prescription p JOIN p.medicationItems mi WHERE LOWER(mi.medicationName) LIKE LOWER(CONCAT('%', :medicationName, '%'))")
    Page<Prescription> findByMedicationName(@Param("medicationName") String medicationName, Pageable pageable);
    
    // Diagnosis queries
    @Query("SELECT p FROM Prescription p WHERE LOWER(p.diagnosis) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    Page<Prescription> findByDiagnosisContaining(@Param("pattern") String pattern, Pageable pageable);
    
    // Appointment queries
    Optional<Prescription> findByAppointmentId(Long appointmentId);
    
    // Patient specific queries
    Page<Prescription> findByPatientIdOrderByIssueDateDesc(Long patientId, Pageable pageable);
    
    @Query("SELECT p FROM Prescription p JOIN p.medicationItems mi WHERE p.patient.id = :patientId AND LOWER(mi.medicationName) LIKE LOWER(CONCAT('%', :medicationName, '%'))")
    Page<Prescription> findByPatientIdAndMedicationName(@Param("patientId") Long patientId, @Param("medicationName") String medicationName, Pageable pageable);
    
    // Print status queries
    Page<Prescription> findByPrintedFalse(Pageable pageable);
    
    // Refill queries
    @Query("SELECT p FROM Prescription p JOIN p.medicationItems mi WHERE mi.refillable = true AND mi.refillsUsed < mi.refillsAllowed")
    Page<Prescription> findPrescriptionsWithRefillableItems(Pageable pageable);
    
    // Renewal queries
    @Query("SELECT p FROM Prescription p WHERE p.status = 'ACTIVE' AND p.issueDate < :cutoffDate")
    Page<Prescription> findPrescriptionsNeedingRenewal(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);
    
    // Drug interaction queries
    @Query("SELECT p FROM Prescription p JOIN p.medicationItems mi WHERE p.patient.id = :patientId AND p.status = 'ACTIVE' AND LOWER(mi.medicationName) LIKE LOWER(CONCAT('%', :medicationName, '%'))")
    Page<Prescription> findActivePrescriptionsWithMedication(@Param("patientId") Long patientId, @Param("medicationName") String medicationName, Pageable pageable);
    
    // Advanced search
    @Query("SELECT p FROM Prescription p WHERE " +
           "(:doctorId IS NULL OR p.doctor.id = :doctorId) AND " +
           "(:patientId IS NULL OR p.patient.id = :patientId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:startDate IS NULL OR p.issueDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.issueDate <= :endDate) AND " +
           "(:diagnosisPattern IS NULL OR LOWER(p.diagnosis) LIKE LOWER(CONCAT('%', :diagnosisPattern, '%'))) AND " +
           "(:medicationName IS NULL OR EXISTS (SELECT mi FROM p.medicationItems mi WHERE LOWER(mi.medicationName) LIKE LOWER(CONCAT('%', :medicationName, '%'))))")
    Page<Prescription> searchPrescriptions(
            @Param("doctorId") Long doctorId,
            @Param("patientId") Long patientId,
            @Param("status") PrescriptionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("diagnosisPattern") String diagnosisPattern,
            @Param("medicationName") String medicationName,
            Pageable pageable);
    
    // Statistics queries
    @Query("SELECT p.status, COUNT(p) FROM Prescription p GROUP BY p.status")
    List<Object[]> countPrescriptionsByStatus();
    
    @Query("SELECT p.doctor.id, p.doctor.firstName, p.doctor.lastName, COUNT(p) FROM Prescription p " +
           "WHERE p.issueDate BETWEEN :startDate AND :endDate " +
           "GROUP BY p.doctor.id, p.doctor.firstName, p.doctor.lastName " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> countPrescriptionsByDoctor(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT mi.medicationName, COUNT(mi) FROM Prescription p JOIN p.medicationItems mi " +
           "GROUP BY mi.medicationName " +
           "ORDER BY COUNT(mi) DESC")
    List<Object[]> findMostPrescribedMedications(Pageable pageable);
}