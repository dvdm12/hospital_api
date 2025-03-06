package com.example.miapp.repository;

import com.example.miapp.models.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing MedicalRecord entities.
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
}
