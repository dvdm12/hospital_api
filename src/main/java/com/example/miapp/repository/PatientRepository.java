package com.example.miapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.miapp.entity.Patient;

import java.util.Optional;

/**
 * Repository for managing Patient entities.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Finds a patient by phone number.
     * @param phone the patient's phone number.
     * @return an Optional containing the patient if found.
     */
    Optional<Patient> findByPhone(String phone);
}
