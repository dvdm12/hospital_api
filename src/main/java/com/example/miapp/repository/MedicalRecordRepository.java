package com.example.miapp.repository;

import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link MedicalRecord} entity.
 * Provides methods for CRUD operations and custom queries related to medical record management.
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    /**
     * Finds a medical record by patient ID.
     *
     * @param patientId the patient ID to search for
     * @return an Optional containing the medical record if found, or empty if not found
     */
    Optional<MedicalRecord> findByPatientId(Long patientId);

    /**
     * Updates the allergies field of a medical record.
     *
     * @param recordId the ID of the medical record to update
     * @param allergies the new allergies information
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE MedicalRecord mr SET mr.allergies = :allergies, mr.updatedAt = CURRENT_TIMESTAMP WHERE mr.id = :recordId")
    int updateAllergies(@Param("recordId") Long recordId, @Param("allergies") String allergies);

    /**
     * Updates the chronic conditions field of a medical record.
     *
     * @param recordId the ID of the medical record to update
     * @param chronicConditions the new chronic conditions information
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE MedicalRecord mr SET mr.chronicConditions = :chronicConditions, mr.updatedAt = CURRENT_TIMESTAMP WHERE mr.id = :recordId")
    int updateChronicConditions(@Param("recordId") Long recordId, @Param("chronicConditions") String chronicConditions);

    /**
     * Updates the current medications field of a medical record.
     *
     * @param recordId the ID of the medical record to update
     * @param currentMedications the new current medications information
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE MedicalRecord mr SET mr.currentMedications = :currentMedications, mr.updatedAt = CURRENT_TIMESTAMP WHERE mr.id = :recordId")
    int updateCurrentMedications(@Param("recordId") Long recordId, @Param("currentMedications") String currentMedications);

    /**
     * Updates the family history field of a medical record.
     *
     * @param recordId the ID of the medical record to update
     * @param familyHistory the new family history information
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE MedicalRecord mr SET mr.familyHistory = :familyHistory, mr.updatedAt = CURRENT_TIMESTAMP WHERE mr.id = :recordId")
    int updateFamilyHistory(@Param("recordId") Long recordId, @Param("familyHistory") String familyHistory);

    /**
     * Updates the surgical history field of a medical record.
     *
     * @param recordId the ID of the medical record to update
     * @param surgicalHistory the new surgical history information
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE MedicalRecord mr SET mr.surgicalHistory = :surgicalHistory, mr.updatedAt = CURRENT_TIMESTAMP WHERE mr.id = :recordId")
    int updateSurgicalHistory(@Param("recordId") Long recordId, @Param("surgicalHistory") String surgicalHistory);

    /**
     * Updates the notes field of a medical record.
     *
     * @param recordId the ID of the medical record to update
     * @param notes the new notes information
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE MedicalRecord mr SET mr.notes = :notes, mr.updatedAt = CURRENT_TIMESTAMP WHERE mr.id = :recordId")
    int updateNotes(@Param("recordId") Long recordId, @Param("notes") String notes);

    /**
     * Finds medical records that contain specific allergies.
     *
     * @param allergyPattern the pattern to search for in allergies
     * @param pageable pagination information
     * @return a Page of medical records matching the allergy pattern
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE LOWER(mr.allergies) LIKE LOWER(CONCAT('%', :allergyPattern, '%'))")
    Page<MedicalRecord> findByAllergiesContaining(@Param("allergyPattern") String allergyPattern, Pageable pageable);

    /**
     * Finds medical records that contain specific chronic conditions.
     *
     * @param conditionPattern the pattern to search for in chronic conditions
     * @param pageable pagination information
     * @return a Page of medical records matching the condition pattern
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE LOWER(mr.chronicConditions) LIKE LOWER(CONCAT('%', :conditionPattern, '%'))")
    Page<MedicalRecord> findByChronicConditionsContaining(@Param("conditionPattern") String conditionPattern, Pageable pageable);

    /**
     * Finds medical records that contain specific medications.
     *
     * @param medicationPattern the pattern to search for in current medications
     * @param pageable pagination information
     * @return a Page of medical records matching the medication pattern
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE LOWER(mr.currentMedications) LIKE LOWER(CONCAT('%', :medicationPattern, '%'))")
    Page<MedicalRecord> findByCurrentMedicationsContaining(@Param("medicationPattern") String medicationPattern, Pageable pageable);

    /**
     * Finds medical records updated within a date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @param pageable pagination information
     * @return a Page of medical records updated within the date range
     */
    Page<MedicalRecord> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Finds medical records with entries by a specific doctor.
     *
     * @param doctorId the ID of the doctor
     * @param pageable pagination information
     * @return a Page of medical records with entries by the specified doctor
     */
    @Query("SELECT DISTINCT mr FROM MedicalRecord mr JOIN mr.entries e WHERE e.doctor.id = :doctorId")
    Page<MedicalRecord> findByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);

    /**
     * Finds medical records with entries of a specific type.
     *
     * @param entryType the type of medical record entry
     * @param pageable pagination information
     * @return a Page of medical records with entries of the specified type
     */
    @Query("SELECT DISTINCT mr FROM MedicalRecord mr JOIN mr.entries e WHERE e.type = :entryType")
    Page<MedicalRecord> findByEntryType(@Param("entryType") MedicalRecordEntry.EntryType entryType, Pageable pageable);

    /**
     * Finds medical records with entries containing specific content.
     *
     * @param contentPattern the pattern to search for in entry content
     * @param pageable pagination information
     * @return a Page of medical records with entries matching the content pattern
     */
    @Query("SELECT DISTINCT mr FROM MedicalRecord mr JOIN mr.entries e WHERE LOWER(e.content) LIKE LOWER(CONCAT('%', :contentPattern, '%'))")
    Page<MedicalRecord> findByEntryContentContaining(@Param("contentPattern") String contentPattern, Pageable pageable);

    /**
     * Finds medical records with entries created within a date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @param pageable pagination information
     * @return a Page of medical records with entries created within the date range
     */
    @Query("SELECT DISTINCT mr FROM MedicalRecord mr JOIN mr.entries e WHERE e.entryDate BETWEEN :startDate AND :endDate")
    Page<MedicalRecord> findByEntryDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Counts the number of entries for each medical record.
     * Returns a list of arrays containing [recordId, patientFirstName, patientLastName, entryCount]
     *
     * @return list of arrays with medical record entry statistics
     */
    @Query("SELECT mr.id, mr.patient.firstName, mr.patient.lastName, COUNT(e) " +
           "FROM MedicalRecord mr LEFT JOIN mr.entries e " +
           "GROUP BY mr.id, mr.patient.firstName, mr.patient.lastName " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> countEntriesByMedicalRecord();

    /**
     * Finds medical records with no entries.
     *
     * @param pageable pagination information
     * @return a Page of medical records with no entries
     */
    @Query("SELECT mr FROM MedicalRecord mr LEFT JOIN mr.entries e WHERE e.id IS NULL")
    Page<MedicalRecord> findMedicalRecordsWithNoEntries(Pageable pageable);

    /**
     * Finds medical records with at least a specified number of entries.
     *
     * @param minEntries the minimum number of entries
     * @param pageable pagination information
     * @return a Page of medical records with at least the specified number of entries
     */
    @Query("SELECT mr FROM MedicalRecord mr JOIN mr.entries e " +
           "GROUP BY mr HAVING COUNT(e) >= :minEntries")
    Page<MedicalRecord> findMedicalRecordsWithMinimumEntries(@Param("minEntries") long minEntries, Pageable pageable);

    /**
     * Advanced search for medical records by multiple criteria.
     *
     * @param allergyPattern optional pattern to search for in allergies
     * @param conditionPattern optional pattern to search for in chronic conditions
     * @param medicationPattern optional pattern to search for in current medications
     * @param entryType optional type of medical record entry
     * @param contentPattern optional pattern to search for in entry content
     * @param pageable pagination information
     * @return a Page of medical records matching the criteria
     */
    @Query("SELECT DISTINCT mr FROM MedicalRecord mr LEFT JOIN mr.entries e " +
           "WHERE (:allergyPattern IS NULL OR LOWER(mr.allergies) LIKE LOWER(CONCAT('%', :allergyPattern, '%'))) " +
           "AND (:conditionPattern IS NULL OR LOWER(mr.chronicConditions) LIKE LOWER(CONCAT('%', :conditionPattern, '%'))) " +
           "AND (:medicationPattern IS NULL OR LOWER(mr.currentMedications) LIKE LOWER(CONCAT('%', :medicationPattern, '%'))) " +
           "AND (:entryType IS NULL OR e.type = :entryType) " +
           "AND (:contentPattern IS NULL OR LOWER(e.content) LIKE LOWER(CONCAT('%', :contentPattern, '%')))")
    Page<MedicalRecord> searchMedicalRecords(
            @Param("allergyPattern") String allergyPattern,
            @Param("conditionPattern") String conditionPattern,
            @Param("medicationPattern") String medicationPattern,
            @Param("entryType") MedicalRecordEntry.EntryType entryType,
            @Param("contentPattern") String contentPattern,
            Pageable pageable);

    /**
     * Custom projection interface for basic medical record information.
     */
    interface MedicalRecordBasicInfo {
        Long getId();
        Long getPatientId();
        String getAllergies();
        String getChronicConditions();
        LocalDateTime getCreatedAt();
        LocalDateTime getUpdatedAt();
    }

    /**
     * Finds all medical records and returns only basic information.
     *
     * @param pageable pagination information
     * @return a Page of MedicalRecordBasicInfo projections
     */
    @Query("SELECT mr.id as id, mr.patient.id as patientId, mr.allergies as allergies, " +
           "mr.chronicConditions as chronicConditions, mr.createdAt as createdAt, " +
           "mr.updatedAt as updatedAt FROM MedicalRecord mr")
    Page<MedicalRecordBasicInfo> findAllBasicInfo(Pageable pageable);
}