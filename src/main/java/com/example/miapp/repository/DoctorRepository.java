package com.example.miapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.miapp.entity.Doctor;

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
