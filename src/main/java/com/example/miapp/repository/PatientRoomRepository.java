package com.example.miapp.repository;

import com.example.miapp.models.PatientRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing PatientRoom entities.
 */
@Repository
public interface PatientRoomRepository extends JpaRepository<PatientRoom, Long> {
}
