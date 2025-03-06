package com.example.miapp.repository;

import com.example.miapp.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing Appointment entities.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Finds all appointments scheduled after a specific date.
     * @param date the starting date.
     * @return a list of appointments.
     */
    List<Appointment> findByDateAfter(LocalDateTime date);
}
