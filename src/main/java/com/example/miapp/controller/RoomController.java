package com.example.miapp.controller;

import com.example.miapp.dto.RoomDto;
import com.example.miapp.services.RoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing room-related operations.
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * Retrieves all rooms.
     *
     * @return List of {@link RoomDto} containing all rooms.
     */
    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    /**
     * Retrieves a room by its ID.
     *
     * @param id The ID of the room.
     * @return {@link RoomDto} containing the requested room details.
     * @throws EntityNotFoundException If the room does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    /**
     * Creates a new room.
     *
     * @param roomDto The data of the new room.
     * @return The created {@link RoomDto}.
     */
    @PostMapping
    public ResponseEntity<RoomDto> createRoom(@Valid @RequestBody RoomDto roomDto) {
        return ResponseEntity.ok(roomService.saveRoom(roomDto));
    }

    /**
     * Updates an existing room.
     *
     * @param id      The ID of the room to update.
     * @param roomDto The new data for the room.
     * @return The updated {@link RoomDto}.
     * @throws EntityNotFoundException If the room does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDto roomDto) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDto));
    }

    /**
     * Deletes a room by its ID.
     *
     * @param id The ID of the room to delete.
     * @return Response with status 204 (No Content) if deletion is successful.
     * @throws EntityNotFoundException If the room does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Handles exceptions related to entity not found errors.
     *
     * @param ex The exception thrown.
     * @return ResponseEntity with status 404 (Not Found) and error message.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
