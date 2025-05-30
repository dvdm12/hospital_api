package com.example.miapp.repository;

import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSpecialty;
import com.example.miapp.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link DoctorSpecialty} entity.
 * Provides methods for CRUD operations and custom queries related to doctor-specialty relationships.
 */
@Repository
public interface DoctorSpecialtyRepository extends JpaRepository<DoctorSpecialty, Long> {

    /**
     * Finds all doctor specialties for a specific doctor.
     *
     * @param doctorId the ID of the doctor
     * @return list of doctor specialties
     */
    List<DoctorSpecialty> findByDoctorId(Long doctorId);

    /**
     * Finds all doctor specialties for a specific specialty.
     *
     * @param specialtyId the ID of the specialty
     * @return list of doctor specialties
     */
    List<DoctorSpecialty> findBySpecialtyId(Long specialtyId);

    /**
     * Finds a doctor specialty by doctor and specialty.
     *
     * @param doctor the doctor entity
     * @param specialty the specialty entity
     * @return an Optional containing the doctor specialty if found, or empty if not found
     */
    Optional<DoctorSpecialty> findByDoctorAndSpecialty(Doctor doctor, Specialty specialty);

    /**
     * Checks if a doctor has a specific specialty.
     *
     * @param doctorId the ID of the doctor
     * @param specialtyId the ID of the specialty
     * @return true if the doctor has the specialty, false otherwise
     */
    @Query("SELECT COUNT(ds) > 0 FROM DoctorSpecialty ds WHERE ds.doctor.id = :doctorId AND ds.specialty.id = :specialtyId")
    boolean existsByDoctorIdAndSpecialtyId(@Param("doctorId") Long doctorId, @Param("specialtyId") Long specialtyId);

    /**
     * Finds doctor specialties by experience level.
     *
     * @param experienceLevel the experience level to search for
     * @return list of doctor specialties with the specified experience level
     */
    List<DoctorSpecialty> findByExperienceLevel(String experienceLevel);

    /**
     * Finds doctor specialties with certification date before a specific date.
     *
     * @param date the date threshold
     * @return list of doctor specialties certified before the specified date
     */
    @Query("SELECT ds FROM DoctorSpecialty ds WHERE ds.certificationDate < :date")
    List<DoctorSpecialty> findByCertificationDateBefore(@Param("date") Date date);

    /**
     * Finds doctor specialties with certification date after a specific date.
     *
     * @param date the date threshold
     * @return list of doctor specialties certified after the specified date
     */
    @Query("SELECT ds FROM DoctorSpecialty ds WHERE ds.certificationDate > :date")
    List<DoctorSpecialty> findByCertificationDateAfter(@Param("date") Date date);

    /**
     * Counts the number of doctors for each specialty.
     * Returns a list of arrays containing [specialtyId, specialtyName, doctorCount]
     *
     * @return list of arrays with specialty statistics
     */
    @Query("SELECT ds.specialty.id, ds.specialty.name, COUNT(DISTINCT ds.doctor) " +
           "FROM DoctorSpecialty ds GROUP BY ds.specialty.id, ds.specialty.name " +
           "ORDER BY COUNT(DISTINCT ds.doctor) DESC")
    List<Object[]> countDoctorsBySpecialty();

    /**
     * Finds specialties for doctors with a specific experience level.
     *
     * @param experienceLevel the experience level to search for
     * @return list of specialties with doctors at the specified experience level
     */
    @Query("SELECT DISTINCT ds.specialty FROM DoctorSpecialty ds WHERE ds.experienceLevel = :experienceLevel")
    List<Specialty> findSpecialtiesByDoctorExperienceLevel(@Param("experienceLevel") String experienceLevel);

    /**
     * Deletes all doctor specialties for a specific doctor.
     *
     * @param doctorId the ID of the doctor
     * @return the number of affected rows
     */
    long deleteByDoctorId(Long doctorId);

    /**
     * Custom MySQL-optimized query to find doctors with multiple specialties.
     * This query uses MySQL 8.0.22 compatible syntax to find doctors who have at least
     * the specified number of specialties.
     *
     * @param specialtyCount the minimum number of specialties
     * @return list of doctors with the specified minimum number of specialties
     */
    @Query(value = "SELECT d.* FROM doctor d " +
                  "INNER JOIN (SELECT doctor_id, COUNT(*) as specialty_count " +
                  "           FROM doctor_specialty " +
                  "           GROUP BY doctor_id " +
                  "           HAVING COUNT(*) >= :specialtyCount) counts " +
                  "ON d.id = counts.doctor_id", 
           nativeQuery = true)
    List<Doctor> findDoctorsWithMinimumSpecialties(@Param("specialtyCount") int specialtyCount);

    /**
     * MySQL-optimized query to find doctors with certain specialties.
     * This query is optimized for MySQL 8.0.22 and uses efficient JOIN operations.
     *
     * @param specialtyIds set of specialty IDs
     * @return list of doctors who have all the specified specialties
     */
    @Query(value = "SELECT d.* FROM doctor d " +
                  "WHERE (SELECT COUNT(DISTINCT ds.specialty_id) " +
                  "       FROM doctor_specialty ds " +
                  "       WHERE ds.doctor_id = d.id " +
                  "       AND ds.specialty_id IN (:specialtyIds)) = :totalSpecialties", 
           nativeQuery = true)
    List<Doctor> findDoctorsWithAllSpecialties(@Param("specialtyIds") List<Long> specialtyIds, 
                                              @Param("totalSpecialties") int totalSpecialties);
}