package com.example.miapp.repository;

import com.example.miapp.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing Room entities.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Finds all available rooms.
     * @param occupancyStatus the status of the room.
     * @return a list of available rooms.
     */
    List<Room> findByOccupancyStatus(String occupancyStatus);
}
