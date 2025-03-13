package com.example.miapp.services;

import com.example.miapp.dto.RoomDto;
import com.example.miapp.models.Room;
import com.example.miapp.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing room-related operations.
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    /**
     * Retrieves all rooms from the database.
     *
     * @return List of {@link RoomDto} containing room details.
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());  
    }

    /**
     * Retrieves a room by its ID.
     *
     * @param id The ID of the room.
     * @return {@link RoomDto} containing room details.
     * @throws EntityNotFoundException If no room is found with the given ID.
     */
    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long id) {
        Room room = findRoomById(id);
        return convertToDto(room);
    }

    /**
     * Saves a new room in the database.
     *
     * @param roomDto The {@link RoomDto} containing the new room details.
     * @return The saved {@link RoomDto}.
     */
    @Transactional
    public RoomDto saveRoom(RoomDto roomDto) {
        Room room = convertToEntity(roomDto);
        return convertToDto(roomRepository.save(room));
    }

    /**
     * Updates an existing room's information.
     *
     * @param id      The ID of the room to be updated.
     * @param roomDto The updated {@link RoomDto} data.
     * @return The updated {@link RoomDto}.
     * @throws EntityNotFoundException If the room does not exist.
     */
    @Transactional
    public RoomDto updateRoom(Long id, RoomDto roomDto) {
        Room existingRoom = findRoomById(id);

        existingRoom = existingRoom.toBuilder()
                .number(roomDto.getNumber())
                .floor(roomDto.getFloor())
                .type(roomDto.getType())
                .occupancyStatus(roomDto.getOccupancyStatus())
                .build();

        return convertToDto(roomRepository.save(existingRoom));
    }

    /**
     * Deletes a room by its ID.
     *
     * @param id The ID of the room to be deleted.
     * @throws EntityNotFoundException If the room does not exist.
     */
    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new EntityNotFoundException("Room not found with ID: " + id);
        }
        roomRepository.deleteById(id);
    }

    /**
     * Finds a room by ID and throws an exception if not found.
     *
     * @param id The ID of the room.
     * @return The {@link Room} entity.
     * @throws EntityNotFoundException If the room does not exist.
     */
    private Room findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with ID: " + id));
    }

    /**
     * Converts a {@link Room} entity to a {@link RoomDto}.
     *
     * @param room The entity to convert.
     * @return The corresponding {@link RoomDto}.
     */
    private RoomDto convertToDto(Room room) {
        return RoomDto.builder()
                .id(room.getId())
                .number(room.getNumber())
                .floor(room.getFloor())
                .type(room.getType())
                .occupancyStatus(room.getOccupancyStatus())
                .build();
    }

    /**
     * Converts a {@link RoomDto} to a {@link Room} entity.
     *
     * @param dto The DTO to convert.
     * @return The corresponding {@link Room} entity.
     */
    private Room convertToEntity(RoomDto dto) {
        return Room.builder()
                .id(dto.getId())
                .number(dto.getNumber())
                .floor(dto.getFloor())
                .type(dto.getType())
                .occupancyStatus(dto.getOccupancyStatus())
                .build();
    }
}
