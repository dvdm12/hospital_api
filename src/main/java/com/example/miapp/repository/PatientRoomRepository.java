package com.example.miapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.miapp.entity.PatientRoom;

/**
 * Repository for managing PatientRoom entities.
 */
@Repository
public interface PatientRoomRepository extends JpaRepository<PatientRoom, Long> {
}
