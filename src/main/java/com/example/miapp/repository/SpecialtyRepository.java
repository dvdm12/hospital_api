package com.example.miapp.repository;

import com.example.miapp.entity.Specialty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Specialty} entity.
 * Provides methods for CRUD operations and custom queries related to medical specialties.
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    /**
     * Finds a specialty by its name.
     *
     * @param name the name to search for
     * @return an Optional containing the specialty if found, or empty if not found
     */
    Optional<Specialty> findByName(String name);

    /**
     * Checks if a specialty with the given name exists.
     *
     * @param name the name to check
     * @return true if a specialty with the name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Finds specialties by name containing the given string (case insensitive).
     *
     * @param namePattern the pattern to match against names
     * @param pageable pagination information
     * @return a Page of specialties with names containing the pattern
     */
    Page<Specialty> findByNameContainingIgnoreCase(String namePattern, Pageable pageable);

    /**
     * Updates a specialty's description.
     *
     * @param specialtyId the ID of the specialty to update
     * @param description the new description
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Specialty s SET s.description = :description WHERE s.id = :specialtyId")
    int updateDescription(@Param("specialtyId") Long specialtyId, @Param("description") String description);

    /**
     * Finds specialties that have doctors assigned to them.
     *
     * @return list of specialties with at least one doctor
     */
    @Query("SELECT DISTINCT s FROM Specialty s JOIN s.doctorSpecialties ds")
    List<Specialty> findSpecialtiesWithDoctors();

    /**
     * Finds specialties that have no doctors assigned to them.
     *
     * @return list of specialties without doctors
     */
    @Query("SELECT s FROM Specialty s LEFT JOIN s.doctorSpecialties ds WHERE ds.id IS NULL")
    List<Specialty> findSpecialtiesWithoutDoctors();

    /**
     * Counts the number of doctors for each specialty.
     * Returns a list of arrays containing [specialtyId, specialtyName, doctorCount]
     *
     * @return list of arrays with specialty statistics
     */
    @Query("SELECT s.id, s.name, COUNT(ds.doctor) FROM Specialty s LEFT JOIN s.doctorSpecialties ds GROUP BY s.id, s.name")
    List<Object[]> countDoctorsBySpecialty();

    /**
     * Finds the most popular specialties based on the number of doctors.
     *
     * @param limit maximum number of specialties to return
     * @return list of specialties ordered by the number of doctors (descending)
     */
    @Query("SELECT s FROM Specialty s LEFT JOIN s.doctorSpecialties ds GROUP BY s ORDER BY COUNT(ds) DESC")
    List<Specialty> findMostPopularSpecialties(Pageable limit);

    /**
     * Finds specialties that have doctors with a specific certification level.
     *
     * @param experienceLevel the experience level to search for
     * @return list of specialties with doctors having the specified experience level
     */
    @Query("SELECT DISTINCT s FROM Specialty s JOIN s.doctorSpecialties ds WHERE ds.experienceLevel = :experienceLevel")
    List<Specialty> findByDoctorExperienceLevel(@Param("experienceLevel") String experienceLevel);

    /**
     * Searches for specialties by name or description.
     *
     * @param searchTerm the term to search for in name or description
     * @param pageable pagination information
     * @return a Page of specialties matching the search term
     */
    @Query("SELECT s FROM Specialty s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Specialty> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Custom projection interface for basic specialty information.
     */
    interface SpecialtyBasicInfo {
        Long getId();
        String getName();
    }

    /**
     * Finds all specialties and returns only basic information.
     *
     * @return list of SpecialtyBasicInfo projections
     */
    @Query("SELECT s.id as id, s.name as name FROM Specialty s")
    List<SpecialtyBasicInfo> findAllBasicInfo();

    /**
     * Finds specialties by doctor ID.
     *
     * @param doctorId the ID of the doctor
     * @return list of specialties associated with the doctor
     */
    @Query("SELECT s FROM Specialty s JOIN s.doctorSpecialties ds WHERE ds.doctor.id = :doctorId")
    List<Specialty> findByDoctorId(@Param("doctorId") Long doctorId);
    
    /**
     * Finds specialties with a minimum number of doctors.
     *
     * @param minDoctorCount minimum number of doctors
     * @return list of specialties with at least the specified number of doctors
     */
    @Query("SELECT s FROM Specialty s JOIN s.doctorSpecialties ds GROUP BY s HAVING COUNT(DISTINCT ds.doctor) >= :minDoctorCount")
    List<Specialty> findSpecialtiesWithMinimumDoctors(@Param("minDoctorCount") long minDoctorCount);

    /**
     * Finds specialties related to a given specialty based on doctors who have both.
     * This can be useful for suggesting related specialties.
     *
     * @param specialtyId the ID of the specialty to find related specialties for
     * @param limit maximum number of related specialties to return
     * @return list of related specialties
     */
    @Query(value = "SELECT s.* FROM specialty s " +
            "JOIN doctor_specialty ds1 ON s.id = ds1.specialty_id " +
            "JOIN doctor d ON ds1.doctor_id = d.id " +
            "JOIN doctor_specialty ds2 ON d.id = ds2.doctor_id " +
            "WHERE ds2.specialty_id = :specialtyId AND s.id != :specialtyId " +
            "GROUP BY s.id " +
            "ORDER BY COUNT(DISTINCT d.id) DESC", 
            nativeQuery = true)
    List<Specialty> findRelatedSpecialties(@Param("specialtyId") Long specialtyId, Pageable limit);
}