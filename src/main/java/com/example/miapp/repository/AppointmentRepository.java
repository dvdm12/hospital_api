package com.example.miapp.repository;

import com.example.miapp.entity.Appointment;
import com.example.miapp.entity.Appointment.AppointmentStatus;
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
 * Repository interface for {@link Appointment} entity.
 * Provides methods for CRUD operations and custom queries related to appointment management.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Finds appointments for a specific patient.
     *
     * @param patientId the patient ID to search for
     * @param pageable pagination information
     * @return a Page of appointments for the specified patient
     */
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    /**
     * Finds appointments with a specific doctor.
     *
     * @param doctorId the doctor ID to search for
     * @param pageable pagination information
     * @return a Page of appointments with the specified doctor
     */
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    /**
     * Finds appointments by status.
     *
     * @param status the status to filter by
     * @param pageable pagination information
     * @return a Page of appointments with the specified status
     */
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    /**
     * Finds appointments in a date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @param pageable pagination information
     * @return a Page of appointments in the specified date range
     */
    Page<Appointment> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Finds the next appointment for a specific patient.
     *
     * @param patientId the patient ID
     * @param currentDate the current date and time
     * @return an Optional containing the next appointment if found, or empty if not found
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND a.date > :currentDate AND a.status NOT IN ('CANCELED', 'COMPLETED', 'NO_SHOW') " +
           "ORDER BY a.date ASC")
    Optional<Appointment> findNextAppointmentForPatient(
            @Param("patientId") Long patientId,
            @Param("currentDate") LocalDateTime currentDate);

    /**
     * Finds appointments for a specific patient with a specific status.
     *
     * @param patientId the patient ID
     * @param status the appointment status
     * @param pageable pagination information
     * @return a Page of appointments matching the criteria
     */
    Page<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status, Pageable pageable);

    /**
     * Finds appointments for a specific doctor with a specific status.
     *
     * @param doctorId the doctor ID
     * @param status the appointment status
     * @param pageable pagination information
     * @return a Page of appointments matching the criteria
     */
    Page<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status, Pageable pageable);

    /**
     * Finds appointments for a specific doctor in a date range.
     *
     * @param doctorId the doctor ID
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @param pageable pagination information
     * @return a Page of appointments matching the criteria
     */
    Page<Appointment> findByDoctorIdAndDateBetween(
            Long doctorId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Finds appointments for a specific patient in a date range.
     *
     * @param patientId the patient ID
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @param pageable pagination information
     * @return a Page of appointments matching the criteria
     */
    Page<Appointment> findByPatientIdAndDateBetween(
            Long patientId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Updates the status of an appointment.
     *
     * @param appointmentId the ID of the appointment to update
     * @param status the new status
     * @param updatedById the ID of the user who performed the update
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Appointment a SET a.status = :status, a.updatedAt = CURRENT_TIMESTAMP, " +
           "a.updatedBy.id = :updatedById WHERE a.id = :appointmentId")
    int updateStatus(
            @Param("appointmentId") Long appointmentId,
            @Param("status") AppointmentStatus status,
            @Param("updatedById") Long updatedById);

    /**
     * Updates the confirmation status of an appointment.
     *
     * @param appointmentId the ID of the appointment to update
     * @param confirmed the new confirmation status
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Appointment a SET a.confirmed = :confirmed, " +
           "a.confirmationDate = CASE WHEN :confirmed = true THEN CURRENT_TIMESTAMP ELSE null END, " +
           "a.updatedAt = CURRENT_TIMESTAMP WHERE a.id = :appointmentId")
    int updateConfirmation(
            @Param("appointmentId") Long appointmentId,
            @Param("confirmed") boolean confirmed);

    /**
     * Updates the notes of an appointment.
     *
     * @param appointmentId the ID of the appointment to update
     * @param notes the new notes
     * @param updatedById the ID of the user who performed the update
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Appointment a SET a.notes = :notes, a.updatedAt = CURRENT_TIMESTAMP, " +
           "a.updatedBy.id = :updatedById WHERE a.id = :appointmentId")
    int updateNotes(
            @Param("appointmentId") Long appointmentId,
            @Param("notes") String notes,
            @Param("updatedById") Long updatedById);

    /**
     * Reschedules an appointment.
     *
     * @param appointmentId the ID of the appointment to reschedule
     * @param newDate the new date and time
     * @param newEndTime the new end time
     * @param updatedById the ID of the user who performed the update
     * @return the number of affected rows
     */
    @Modifying
    @Query("UPDATE Appointment a SET a.date = :newDate, a.endTime = :newEndTime, " +
           "a.status = 'RESCHEDULED', a.updatedAt = CURRENT_TIMESTAMP, " +
           "a.updatedBy.id = :updatedById WHERE a.id = :appointmentId")
    int rescheduleAppointment(
            @Param("appointmentId") Long appointmentId,
            @Param("newDate") LocalDateTime newDate,
            @Param("newEndTime") LocalDateTime newEndTime,
            @Param("updatedById") Long updatedById);

    /**
     * Finds overlapping appointments for a doctor.
     *
     * @param doctorId the doctor ID
     * @param startDate the start date and time
     * @param endDate the end date and time
     * @param excludeAppointmentId optional appointment ID to exclude (can be null)
     * @return list of overlapping appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.status NOT IN ('CANCELED', 'NO_SHOW') " +
           "AND ((a.date < :endDate AND a.endTime > :startDate) " +
           "     OR (a.date = :startDate) OR (a.endTime = :endDate)) " +
           "AND (:excludeAppointmentId IS NULL OR a.id != :excludeAppointmentId)")
    List<Appointment> findOverlappingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("excludeAppointmentId") Long excludeAppointmentId);

    /**
     * Finds appointments that should be marked as "no-show".
     * These are appointments that were scheduled for a time in the past,
     * but were not marked as completed or canceled.
     *
     * @param thresholdTime the time threshold (current time minus grace period)
     * @param pageable pagination information
     * @return a Page of appointments to be marked as no-show
     */
    @Query("SELECT a FROM Appointment a WHERE a.status = 'SCHEDULED' AND a.date < ?1")
    Page<Appointment> findAppointmentsToMarkAsNoShow(
            LocalDateTime thresholdTime,
            Pageable pageable);

    /**
     * Counts appointments by status.
     *
     * @return a list of arrays containing [status, count]
     */
    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatus();

    /**
     * Counts appointments by day of week.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of arrays containing [dayOfWeek, count]
     */
    @Query("SELECT FUNCTION('DAYOFWEEK', a.date), COUNT(a) " +
           "FROM Appointment a " +
           "WHERE a.date BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DAYOFWEEK', a.date)")
    List<Object[]> countAppointmentsByDayOfWeek(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Counts appointments by hour of day.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of arrays containing [hourOfDay, count]
     */
    @Query("SELECT FUNCTION('HOUR', a.date), COUNT(a) " +
           "FROM Appointment a " +
           "WHERE a.date BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('HOUR', a.date)")
    List<Object[]> countAppointmentsByHourOfDay(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Finds appointments for a specific doctor on a specific day of week.
     *
     * @param doctorId the doctor ID
     * @param dayOfWeek the day of week (1-7, where 1 is Sunday)
     * @param pageable pagination information
     * @return a Page of appointments matching the criteria
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND FUNCTION('DAYOFWEEK', a.date) = :dayOfWeek")
    Page<Appointment> findByDoctorIdAndDayOfWeek(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") int dayOfWeek,
            Pageable pageable);

    /**
     * Finds available time slots for a doctor on a specific date.
     * This is a complex query that:
     * 1. Finds the doctor's schedule for the day of week
     * 2. Finds all appointments for the doctor on that date
     * 3. Returns time slots that don't overlap with existing appointments
     *
     * @param doctorId the doctor ID
     * @param date the date to check
     * @return a list of available time slots as [startTime, endTime]
     */
    @Query(nativeQuery = true, 
           value = "WITH time_slots AS ( " +
                  "SELECT " +
                  "    ds.start_time as slot_start, " +
                  "    ADDTIME(ds.start_time, SEC_TO_TIME(ds.slot_duration_minutes * 60)) as slot_end " +
                  "FROM doctor_schedule ds " +
                  "WHERE ds.doctor_id = :doctorId " +
                  "    AND ds.active = TRUE " +
                  "    AND ds.day_of_week = UPPER(DATE_FORMAT(:date, '%W')) " +
                  "    AND ADDTIME(ds.start_time, SEC_TO_TIME(ds.slot_duration_minutes * 60)) <= ds.end_time " +
                  "), " +
                  "booked_slots AS ( " +
                  "SELECT " +
                  "    TIME(a.date) as booked_start, " +
                  "    TIME(a.end_time) as booked_end " +
                  "FROM appointment a " +
                  "WHERE a.doctor_id = :doctorId " +
                  "    AND DATE(a.date) = DATE(:date) " +
                  "    AND a.status NOT IN ('CANCELED', 'NO_SHOW') " +
                  ") " +
                  "SELECT ts.slot_start, ts.slot_end " +
                  "FROM time_slots ts " +
                  "WHERE NOT EXISTS ( " +
                  "    SELECT 1 FROM booked_slots bs " +
                  "    WHERE (bs.booked_start < ts.slot_end AND bs.booked_end > ts.slot_start) " +
                  "        OR bs.booked_start = ts.slot_start " +
                  ") " +
                  "ORDER BY ts.slot_start")
    List<Object[]> findAvailableTimeSlots(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDateTime date);

    /**
     * Advanced search for appointments by multiple criteria.
     *
     * @param doctorId optional doctor ID to filter by
     * @param patientId optional patient ID to filter by
     * @param status optional status to filter by
     * @param startDate optional start date for range filter
     * @param endDate optional end date for range filter
     * @param reasonPattern optional pattern to search for in reason
     * @param pageable pagination information
     * @return a Page of appointments matching the criteria
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE (:doctorId IS NULL OR a.doctor.id = :doctorId) " +
           "AND (:patientId IS NULL OR a.patient.id = :patientId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:startDate IS NULL OR a.date >= :startDate) " +
           "AND (:endDate IS NULL OR a.date <= :endDate) " +
           "AND (:reasonPattern IS NULL OR LOWER(a.reason) LIKE LOWER(CONCAT('%', :reasonPattern, '%')))")
    Page<Appointment> searchAppointments(
            @Param("doctorId") Long doctorId,
            @Param("patientId") Long patientId,
            @Param("status") AppointmentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("reasonPattern") String reasonPattern,
            Pageable pageable);

    /**
     * Custom projection interface for basic appointment information.
     */
    interface AppointmentBasicInfo {
        Long getId();
        LocalDateTime getDate();
        LocalDateTime getEndTime();
        String getDoctorName();
        String getPatientName();
        String getReason();
        AppointmentStatus getStatus();
        boolean isConfirmed();
    }

    /**
     * Finds all appointments and returns only basic information.
     *
     * @param pageable pagination information
     * @return a Page of AppointmentBasicInfo projections
     */
    @Query("SELECT a.id as id, a.date as date, a.endTime as endTime, " +
           "CONCAT(a.doctor.firstName, ' ', a.doctor.lastName) as doctorName, " +
           "CONCAT(a.patient.firstName, ' ', a.patient.lastName) as patientName, " +
           "a.reason as reason, a.status as status, a.confirmed as confirmed " +
           "FROM Appointment a")
    Page<AppointmentBasicInfo> findAllBasicInfo(Pageable pageable);

    /**
     * Finds basic information for appointments for a specific doctor.
     *
     * @param doctorId the doctor ID
     * @param pageable pagination information
     * @return a Page of AppointmentBasicInfo projections
     */
    @Query("SELECT a.id as id, a.date as date, a.endTime as endTime, " +
           "CONCAT(a.doctor.firstName, ' ', a.doctor.lastName) as doctorName, " +
           "CONCAT(a.patient.firstName, ' ', a.patient.lastName) as patientName, " +
           "a.reason as reason, a.status as status, a.confirmed as confirmed " +
           "FROM Appointment a WHERE a.doctor.id = :doctorId")
    Page<AppointmentBasicInfo> findBasicInfoByDoctorId(
            @Param("doctorId") Long doctorId,
            Pageable pageable);

    /**
     * Finds basic information for appointments for a specific patient.
     *
     * @param patientId the patient ID
     * @param pageable pagination information
     * @return a Page of AppointmentBasicInfo projections
     */
    @Query("SELECT a.id as id, a.date as date, a.endTime as endTime, " +
           "CONCAT(a.doctor.firstName, ' ', a.doctor.lastName) as doctorName, " +
           "CONCAT(a.patient.firstName, ' ', a.patient.lastName) as patientName, " +
           "a.reason as reason, a.status as status, a.confirmed as confirmed " +
           "FROM Appointment a WHERE a.patient.id = :patientId")
    Page<AppointmentBasicInfo> findBasicInfoByPatientId(
            @Param("patientId") Long patientId,
            Pageable pageable);
}