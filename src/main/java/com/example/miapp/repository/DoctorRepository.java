package com.example.miapp.repository;

import com.example.miapp.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Doctor} entity.
 * Provides methods for CRUD operations and custom queries related to doctor management.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Finds a doctor by email.
     *
     * @param email the email to search for
     * @return an Optional containing the doctor if found, or empty if not found
     */
    Optional<Doctor> findByEmail(String email);

    /**
     * Finds a doctor by license number.
     *
     * @param licenseNumber the license number to search for
     * @return an Optional containing the doctor if found, or empty if not found
     */
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    /**
     * Finds a doctor by associated user ID.
     *
     * @param userId the user ID to search for
     * @return an Optional containing the doctor if found, or empty if not found
     */
    Optional<Doctor> findByUserId(Long userId);

    /**
     * Finds doctors by first name and last name (case insensitive).
     *
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @param pageable pagination information
     * @return a Page of doctors matching the name criteria
     */
    Page<Doctor> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    /**
     * Finds doctors by name pattern (case insensitive).
     *
     * @param namePattern the pattern to match against first name or last name
     * @param pageable pagination information
     * @return a Page of doctors matching the name pattern
     */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
           "OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :namePattern, '%'))")
    Page<Doctor> findByNameContaining(@Param("namePattern") String namePattern, Pageable pageable);

    /**
     * Finds doctors by specialty ID.
     *
     * @param specialtyId the specialty ID to search for
     * @param pageable pagination information
     * @return a Page of doctors with the specified specialty
     */
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.doctorSpecialties ds WHERE ds.specialty.id = :specialtyId")
    Page<Doctor> findBySpecialtyId(@Param("specialtyId") Long specialtyId, Pageable pageable);

    /**
     * Finds doctors by multiple specialty IDs.
     *
     * @param specialtyIds the list of specialty IDs to search for
     * @param pageable pagination information
     * @return a Page of doctors with any of the specified specialties
     */
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.doctorSpecialties ds WHERE ds.specialty.id IN :specialtyIds")
    Page<Doctor> findBySpecialtyIdIn(@Param("specialtyIds") List<Long> specialtyIds, Pageable pageable);

    /**
     * Updates a doctor's consultation fee.
     *
     * @param doctorId the ID of the doctor to update
     * @param consultationFee the new consultation fee
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Doctor d SET d.consultationFee = :consultationFee WHERE d.id = :doctorId")
    int updateConsultationFee(@Param("doctorId") Long doctorId, @Param("consultationFee") Double consultationFee);

    /**
     * Updates a doctor's biography.
     *
     * @param doctorId the ID of the doctor to update
     * @param biography the new biography
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Doctor d SET d.biography = :biography WHERE d.id = :doctorId")
    int updateBiography(@Param("doctorId") Long doctorId, @Param("biography") String biography);

    /**
     * Finds doctors available on a specific day of the week and time.
     *
     * @param dayOfWeek the day of week to check
     * @param startTime the start time to check
     * @param endTime the end time to check
     * @param pageable pagination information
     * @return a Page of available doctors
     */
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.workSchedules ws " +
           "WHERE ws.dayOfWeek = :dayOfWeek " +
           "AND ws.startTime <= :startTime " +
           "AND ws.endTime >= :endTime " +
           "AND ws.active = true")
    Page<Doctor> findAvailableDoctors(
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            Pageable pageable);

    /**
     * Finds doctors who have appointments in a given date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @param pageable pagination information
     * @return a Page of doctors with appointments in the date range
     */
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.appointments a " +
           "WHERE a.date BETWEEN :startDate AND :endDate")
    Page<Doctor> findWithAppointmentsInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Counts the number of appointments for each doctor in a date range.
     * Returns a list of arrays containing [doctorId, firstName, lastName, appointmentCount]
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of arrays with doctor appointment statistics
     */
    @Query("SELECT d.id, d.firstName, d.lastName, COUNT(a) " +
           "FROM Doctor d LEFT JOIN d.appointments a " +
           "WHERE a.date IS NULL OR (a.date BETWEEN :startDate AND :endDate) " +
           "GROUP BY d.id, d.firstName, d.lastName " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countAppointmentsByDoctor(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Finds doctors with the highest consultation fees.
     *
     * @param pageable pagination information
     * @return a Page of doctors ordered by consultation fee (descending)
     */
    @Query("SELECT d FROM Doctor d WHERE d.consultationFee IS NOT NULL ORDER BY d.consultationFee DESC")
    Page<Doctor> findByHighestConsultationFee(Pageable pageable);

    /**
     * Finds doctors with the lowest consultation fees.
     *
     * @param pageable pagination information
     * @return a Page of doctors ordered by consultation fee (ascending)
     */
    @Query("SELECT d FROM Doctor d WHERE d.consultationFee IS NOT NULL ORDER BY d.consultationFee ASC")
    Page<Doctor> findByLowestConsultationFee(Pageable pageable);

    /**
     * Finds doctors within a consultation fee range.
     *
     * @param minFee the minimum fee
     * @param maxFee the maximum fee
     * @param pageable pagination information
     * @return a Page of doctors within the fee range
     */
    @Query("SELECT d FROM Doctor d WHERE d.consultationFee BETWEEN :minFee AND :maxFee")
    Page<Doctor> findByConsultationFeeBetween(
            @Param("minFee") Double minFee,
            @Param("maxFee") Double maxFee,
            Pageable pageable);

    /**
     * Finds doctors with a specific experience level in any specialty.
     *
     * @param experienceLevel the experience level to search for
     * @param pageable pagination information
     * @return a Page of doctors with the specified experience level
     */
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.doctorSpecialties ds " +
           "WHERE ds.experienceLevel = :experienceLevel")
    Page<Doctor> findByExperienceLevel(
            @Param("experienceLevel") String experienceLevel,
            Pageable pageable);

    /**
     * Advanced search for doctors by multiple criteria.
     *
     * @param name optional name pattern to search for
     * @param specialtyId optional specialty ID to filter by
     * @param dayOfWeek optional day of week for availability
     * @param minFee optional minimum consultation fee
     * @param maxFee optional maximum consultation fee
     * @param pageable pagination information
     * @return a Page of doctors matching the criteria
     */
    @Query("SELECT DISTINCT d FROM Doctor d " +
           "LEFT JOIN d.doctorSpecialties ds " +
           "LEFT JOIN d.workSchedules ws " +
           "WHERE (:name IS NULL OR LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "        OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:specialtyId IS NULL OR ds.specialty.id = :specialtyId) " +
           "AND (:dayOfWeek IS NULL OR ws.dayOfWeek = :dayOfWeek) " +
           "AND (:minFee IS NULL OR d.consultationFee >= :minFee) " +
           "AND (:maxFee IS NULL OR d.consultationFee <= :maxFee)")
    Page<Doctor> searchDoctors(
            @Param("name") String name,
            @Param("specialtyId") Long specialtyId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("minFee") Double minFee,
            @Param("maxFee") Double maxFee,
            Pageable pageable);

    /**
     * Custom projection interface for basic doctor information.
     */
    interface DoctorBasicInfo {
        Long getId();
        String getFirstName();
        String getLastName();
        String getEmail();
        String getPhone();
        Double getConsultationFee();
    }

    /**
     * Finds all doctors and returns only basic information.
     *
     * @param pageable pagination information
     * @return a Page of DoctorBasicInfo projections
     */
    @Query("SELECT d.id as id, d.firstName as firstName, d.lastName as lastName, " +
           "d.email as email, d.phone as phone, d.consultationFee as consultationFee " +
           "FROM Doctor d")
    Page<DoctorBasicInfo> findAllBasicInfo(Pageable pageable);

    /**
     * Finds doctors with at least the specified number of specialties.
     *
     * @param specialtyCount minimum number of specialties
     * @param pageable pagination information
     * @return a Page of doctors with at least the specified number of specialties
     */
    @Query("SELECT d FROM Doctor d JOIN d.doctorSpecialties ds " +
           "GROUP BY d HAVING COUNT(DISTINCT ds.specialty) >= :specialtyCount")
    Page<Doctor> findDoctorsWithMinimumSpecialties(
            @Param("specialtyCount") long specialtyCount,
            Pageable pageable);

    /**
     * Finds doctors who don't have a specific specialty.
     *
     * @param specialtyId the specialty ID to exclude
     * @param pageable pagination information
     * @return a Page of doctors without the specified specialty
     */
    @Query("SELECT d FROM Doctor d WHERE d.id NOT IN " +
           "(SELECT d2.id FROM Doctor d2 JOIN d2.doctorSpecialties ds WHERE ds.specialty.id = :specialtyId)")
    Page<Doctor> findDoctorsWithoutSpecialty(
            @Param("specialtyId") Long specialtyId,
            Pageable pageable);
}