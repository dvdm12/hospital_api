package com.example.miapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.miapp.entity.Specialty;

/**
 * Repository for managing Specialty entities.
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
}
