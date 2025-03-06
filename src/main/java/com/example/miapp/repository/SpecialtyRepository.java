package com.example.miapp.repository;

import com.example.miapp.models.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Specialty entities.
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
}
