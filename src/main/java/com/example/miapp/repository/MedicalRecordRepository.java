package com.example.miapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.miapp.entity.MedicalRecord;

/**
 * Repository for managing MedicalRecord entities.
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
}
