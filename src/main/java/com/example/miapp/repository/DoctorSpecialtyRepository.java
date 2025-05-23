package com.example.miapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.miapp.entity.DoctorSpecialty;

/**
 * Repository for managing DoctorSpecialty entities.
 */
@Repository
public interface DoctorSpecialtyRepository extends JpaRepository<DoctorSpecialty, Long> {
}
