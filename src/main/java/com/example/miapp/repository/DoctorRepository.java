package com.example.miapp.repository;

import com.example.miapp.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing Doctor entities.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Finds a doctor by email.
     * @param email the doctor's email.
     * @return an Optional containing the doctor if found.
     */
    Optional<Doctor> findByEmail(String email);
}
