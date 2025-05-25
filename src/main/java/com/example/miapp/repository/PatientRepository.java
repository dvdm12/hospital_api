package com.example.miapp.repository;

import com.example.miapp.entity.Patient;
import com.example.miapp.entity.Patient.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Patient} entity.
 * Provides methods for CRUD operations and custom queries related to patient management.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Finds a patient by email.
     *
     * @param email the email to search for
     * @return an Optional containing the patient if found, or empty if not found
     */
    @Query("SELECT p FROM Patient p JOIN p.user u WHERE u.email = :email")
    Optional<Patient> findByEmail(String email);

    /**
     * Finds a patient by phone number.
     *
     * @param phone the phone number to search for
     * @return an Optional containing the patient if found, or empty if not found
     */
    Optional<Patient> findByPhone(String phone);

    /**
     * Finds a patient by associated user ID.
     *
     * @param userId the user ID to search for
     * @return an Optional containing the patient if found, or empty if not found
     */
    Optional<Patient> findByUserId(Long userId);

    /**
     * Finds patients by first name and last name (case insensitive).
     *
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @param pageable pagination information
     * @return a Page of patients matching the name criteria
     */
    Page<Patient> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    /**
     * Finds patients by name pattern (case insensitive).
     *
     * @param namePattern the pattern to match against first name or last name
     * @param pageable pagination information
     * @return a Page of patients matching the name pattern
     */
    @Query("SELECT p FROM Patient p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
           "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :namePattern, '%'))")
    Page<Patient> findByNameContaining(@Param("namePattern") String namePattern, Pageable pageable);

    /**
     * Finds patients by gender.
     *
     * @param gender the gender to filter by
     * @param pageable pagination information
     * @return a Page of patients with the specified gender
     */
    Page<Patient> findByGender(Gender gender, Pageable pageable);

    /**
     * Finds patients by birth date before a specific date.
     *
     * @param date the date to compare against
     * @param pageable pagination information
     * @return a Page of patients born before the specified date
     */
    Page<Patient> findByBirthDateBefore(Date date, Pageable pageable);

    /**
     * Finds patients by birth date after a specific date.
     *
     * @param date the date to compare against
     * @param pageable pagination information
     * @return a Page of patients born after the specified date
     */
    Page<Patient> findByBirthDateAfter(Date date, Pageable pageable);

    /**
     * Finds patients by age range.
     *
     * @param startDate the end date for the older age bound
     * @param endDate the end date for the younger age bound
     * @param pageable pagination information
     * @return a Page of patients within the age range
     */
    @Query("SELECT p FROM Patient p WHERE p.birthDate BETWEEN :startDate AND :endDate")
    Page<Patient> findByAgeRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            Pageable pageable);

    /**
     * Finds patients by insurance provider.
     *
     * @param insuranceProvider the insurance provider to search for
     * @param pageable pagination information
     * @return a Page of patients with the specified insurance provider
     */
    Page<Patient> findByInsuranceProviderContainingIgnoreCase(
            String insuranceProvider, Pageable pageable);

    /**
     * Updates a patient's address.
     *
     * @param patientId the ID of the patient to update
     * @param address the new address
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Patient p SET p.address = :address WHERE p.id = :patientId")
    int updateAddress(@Param("patientId") Long patientId, @Param("address") String address);

    /**
     * Updates a patient's phone number.
     *
     * @param patientId the ID of the patient to update
     * @param phone the new phone number
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Patient p SET p.phone = :phone WHERE p.id = :patientId")
    int updatePhone(@Param("patientId") Long patientId, @Param("phone") String phone);

    /**
     * Updates a patient's insurance information.
     *
     * @param patientId the ID of the patient to update
     * @param insuranceProvider the new insurance provider
     * @param insurancePolicyNumber the new insurance policy number
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Patient p SET p.insuranceProvider = :insuranceProvider, " +
           "p.insurancePolicyNumber = :insurancePolicyNumber WHERE p.id = :patientId")
    int updateInsuranceInfo(
            @Param("patientId") Long patientId,
            @Param("insuranceProvider") String insuranceProvider,
            @Param("insurancePolicyNumber") String insurancePolicyNumber);

    /**
     * Updates a patient's emergency contact information.
     *
     * @param patientId the ID of the patient to update
     * @param emergencyContactName the new emergency contact name
     * @param emergencyContactPhone the new emergency contact phone
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Patient p SET p.emergencyContactName = :emergencyContactName, " +
           "p.emergencyContactPhone = :emergencyContactPhone WHERE p.id = :patientId")
    int updateEmergencyContact(
            @Param("patientId") Long patientId,
            @Param("emergencyContactName") String emergencyContactName,
            @Param("emergencyContactPhone") String emergencyContactPhone);

    /**
     * Finds patients who have appointments with a specific doctor.
     *
     * @param doctorId the ID of the doctor
     * @param pageable pagination information
     * @return a Page of patients with appointments with the specified doctor
     */
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.appointments a WHERE a.doctor.id = :doctorId")
    Page<Patient> findByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);

    /**
     * Finds patients who have prescriptions for a specific medication.
     *
     * @param medicationName the name of the medication
     * @param pageable pagination information
     * @return a Page of patients with prescriptions for the specified medication
     */
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.prescriptions pr JOIN pr.medicationItems mi " +
           "WHERE LOWER(mi.medicationName) LIKE LOWER(CONCAT('%', :medicationName, '%'))")
    Page<Patient> findByMedicationName(
            @Param("medicationName") String medicationName,
            Pageable pageable);

    /**
     * Finds patients with medical record entries of a specific type.
     *
     * @param entryType the type of medical record entry
     * @param pageable pagination information
     * @return a Page of patients with medical record entries of the specified type
     */
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.medicalRecord mr JOIN mr.entries e " +
           "WHERE e.type = :entryType")
    Page<Patient> findByMedicalRecordEntryType(
            @Param("entryType") String entryType,
            Pageable pageable);

    /**
     * Finds patients with chronic conditions containing the given text.
     *
     * @param condition the condition text to search for
     * @param pageable pagination information
     * @return a Page of patients with matching chronic conditions
     */
    @Query("SELECT p FROM Patient p JOIN p.medicalRecord mr " +
           "WHERE LOWER(mr.chronicConditions) LIKE LOWER(CONCAT('%', :condition, '%'))")
    Page<Patient> findByChronicCondition(
            @Param("condition") String condition,
            Pageable pageable);

    /**
     * Finds patients with allergies containing the given text.
     *
     * @param allergy the allergy text to search for
     * @param pageable pagination information
     * @return a Page of patients with matching allergies
     */
    @Query("SELECT p FROM Patient p JOIN p.medicalRecord mr " +
           "WHERE LOWER(mr.allergies) LIKE LOWER(CONCAT('%', :allergy, '%'))")
    Page<Patient> findByAllergy(
            @Param("allergy") String allergy,
            Pageable pageable);

    /**
     * Advanced search for patients by multiple criteria.
     *
     * @param name optional name pattern to search for
     * @param gender optional gender to filter by
     * @param minAge optional minimum age
     * @param maxAge optional maximum age
     * @param insuranceProvider optional insurance provider
     * @param condition optional chronic condition
     * @param pageable pagination information
     * @return a Page of patients matching the criteria
     */
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN p.medicalRecord mr " +
           "WHERE (:name IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "        OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:gender IS NULL OR p.gender = :gender) " +
           "AND (:minAge IS NULL OR FUNCTION('TIMESTAMPDIFF', YEAR, p.birthDate, CURRENT_DATE) >= :minAge) " +
           "AND (:maxAge IS NULL OR FUNCTION('TIMESTAMPDIFF', YEAR, p.birthDate, CURRENT_DATE) <= :maxAge) " +
           "AND (:insuranceProvider IS NULL OR LOWER(p.insuranceProvider) LIKE LOWER(CONCAT('%', :insuranceProvider, '%'))) " +
           "AND (:condition IS NULL OR LOWER(mr.chronicConditions) LIKE LOWER(CONCAT('%', :condition, '%')))")
    Page<Patient> searchPatients(
            @Param("name") String name,
            @Param("gender") Gender gender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("insuranceProvider") String insuranceProvider,
            @Param("condition") String condition,
            Pageable pageable);

    /**
     * Counts patients by gender.
     *
     * @return a list of arrays containing [gender, count]
     */
    @Query("SELECT p.gender, COUNT(p) FROM Patient p GROUP BY p.gender")
    List<Object[]> countPatientsByGender();

    /**
     * Counts patients by age group.
     *
     * @param interval the age interval for grouping
     * @return a list of arrays containing [ageGroup, count]
     */
    @Query(value = "SELECT CONCAT(FLOOR(TIMESTAMPDIFF(YEAR, birth_date, CURDATE()) / ?1) * ?1, '-', " +
                   "FLOOR(TIMESTAMPDIFF(YEAR, birth_date, CURDATE()) / ?1) * ?1 + ?1 - 1) AS age_group, " +
                   "COUNT(*) AS count FROM patient GROUP BY age_group ORDER BY MIN(TIMESTAMPDIFF(YEAR, birth_date, CURDATE()))",
           nativeQuery = true)
    List<Object[]> countPatientsByAgeGroup(int interval);

    /**
     * Custom projection interface for basic patient information.
     */
    interface PatientBasicInfo {
        Long getId();
        String getFirstName();
        String getLastName();
        Date getBirthDate();
        String getPhone();
        Gender getGender();
    }

    /**
     * Finds all patients and returns only basic information.
     *
     * @param pageable pagination information
     * @return a Page of PatientBasicInfo projections
     */
    @Query("SELECT p.id as id, p.firstName as firstName, p.lastName as lastName, " +
           "p.birthDate as birthDate, p.phone as phone, p.gender as gender FROM Patient p")
    Page<PatientBasicInfo> findAllBasicInfo(Pageable pageable);

    /**
     * Finds patients who haven't had an appointment in a long time.
     *
     * @param monthsThreshold the number of months to consider as "a long time"
     * @param pageable pagination information
     * @return a Page of patients without recent appointments
     */
    @Query("SELECT p FROM Patient p WHERE p.id NOT IN " +
           "(SELECT DISTINCT a.patient.id FROM Appointment a " +
           "WHERE a.date > FUNCTION('DATE_SUB', CURRENT_TIMESTAMP, FUNCTION('INTERVAL', ?1)))")
    Page<Patient> findPatientsWithoutRecentAppointments(String interval, Pageable pageable);

    /**
     * Finds new patients registered within a recent time period.
     *
     * @param interval the interval string (e.g., '6 MONTH')
     * @param pageable pagination information
     * @return a Page of recently registered patients
     */
    @Query("SELECT p FROM Patient p JOIN p.user u " +
           "WHERE u.id IN (SELECT u2.id FROM User u2 WHERE u2.firstLogin = true) " +
           "AND u.lastLogin > FUNCTION('DATE_SUB', CURRENT_TIMESTAMP, FUNCTION('INTERVAL', ?1))")
    Page<Patient> findNewPatients(String interval, Pageable pageable);
}