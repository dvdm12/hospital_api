package com.example.miapp.services;

import com.example.miapp.dto.PatientRoomDto;
import com.example.miapp.entity.Patient;
import com.example.miapp.entity.PatientRoom;
import com.example.miapp.entity.Room;
import com.example.miapp.repository.PatientRepository;
import com.example.miapp.repository.PatientRoomRepository;
import com.example.miapp.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing patient-room relation operations.
 */
@Service
@RequiredArgsConstructor
public class PatientRoomService {

    private final PatientRoomRepository patientRoomRepository;
    private final PatientRepository patientRepository;
    private final RoomRepository roomRepository;

    /**
     * Retrieves all patient-room relations from the database.
     *
     * @return List of {@link PatientRoomDto} containing details of patient-room assignments.
     */
    @Transactional(readOnly = true)
    public List<PatientRoomDto> getAllPatientRooms() {
        return patientRoomRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a patient-room relation by its ID.
     *
     * @param id The ID of the patient-room relation.
     * @return {@link PatientRoomDto} containing patient-room details.
     * @throws EntityNotFoundException If no relation is found with the given ID.
     */
    @Transactional(readOnly = true)
    public PatientRoomDto getPatientRoomById(Long id) {
        PatientRoom patientRoom = findPatientRoomById(id);
        return convertToDto(patientRoom);
    }

    /**
     * Saves a new patient-room relation in the database.
     *
     * @param patientRoomDto The {@link PatientRoomDto} containing the new patient-room details.
     * @return The saved {@link PatientRoomDto}.
     * @throws EntityNotFoundException If the associated patient or room does not exist.
     * @throws IllegalArgumentException If the check-out date is before the check-in date.
     */
    @Transactional
    public PatientRoomDto savePatientRoom(PatientRoomDto patientRoomDto) {
        Patient patient = findPatientById(patientRoomDto.getPatientId());
        Room room = findRoomById(patientRoomDto.getRoomId());

        validateDates(patientRoomDto.getCheckInDate(), patientRoomDto.getCheckOutDate());

        PatientRoom patientRoom = convertToEntity(patientRoomDto);
        patientRoom.setPatient(patient);
        patientRoom.setRoom(room);

        return convertToDto(patientRoomRepository.save(patientRoom));
    }

    /**
     * Updates an existing patient-room relation.
     *
     * @param id             The ID of the patient-room relation to be updated.
     * @param patientRoomDto The updated {@link PatientRoomDto} data.
     * @return The updated {@link PatientRoomDto}.
     * @throws EntityNotFoundException If the relation, patient, or room does not exist.
     * @throws IllegalArgumentException If the check-out date is before the check-in date.
     */
    @Transactional
    public PatientRoomDto updatePatientRoom(Long id, PatientRoomDto patientRoomDto) {
        PatientRoom existingPatientRoom = findPatientRoomById(id);

        Patient patient = findPatientById(patientRoomDto.getPatientId());
        Room room = findRoomById(patientRoomDto.getRoomId());

        validateDates(patientRoomDto.getCheckInDate(), patientRoomDto.getCheckOutDate());

        existingPatientRoom = existingPatientRoom.toBuilder()
                .patient(patient)
                .room(room)
                .checkInDate(patientRoomDto.getCheckInDate())
                .checkOutDate(patientRoomDto.getCheckOutDate())
                .observations(patientRoomDto.getObservations())
                .build();

        return convertToDto(patientRoomRepository.save(existingPatientRoom));
    }

    /**
     * Deletes a patient-room relation by its ID.
     *
     * @param id The ID of the relation to be deleted.
     * @throws EntityNotFoundException If the relation does not exist.
     */
    @Transactional
    public void deletePatientRoom(Long id) {
        if (!patientRoomRepository.existsById(id)) {
            throw new EntityNotFoundException("PatientRoom not found with ID: " + id);
        }
        patientRoomRepository.deleteById(id);
    }

    /**
     * Finds a patient-room relation by ID and throws an exception if not found.
     *
     * @param id The ID of the patient-room relation.
     * @return The {@link PatientRoom} entity.
     * @throws EntityNotFoundException If the relation does not exist.
     */
    private PatientRoom findPatientRoomById(Long id) {
        return patientRoomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PatientRoom not found with ID: " + id));
    }

    /**
     * Finds a patient by ID and throws an exception if not found.
     *
     * @param id The ID of the patient.
     * @return The {@link Patient} entity.
     * @throws EntityNotFoundException If the patient does not exist.
     */
    private Patient findPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with ID: " + id));
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
     * Validates that the check-out date is after the check-in date.
     *
     * @param checkInDate  The check-in date.
     * @param checkOutDate The check-out date.
     * @throws IllegalArgumentException If the check-out date is before the check-in date.
     */
    private void validateDates(java.util.Date checkInDate, java.util.Date checkOutDate) {
        if (checkOutDate != null && checkInDate != null && checkOutDate.before(checkInDate)) {
            throw new IllegalArgumentException("Check-out date cannot be before check-in date.");
        }
    }

    /**
     * Converts a {@link PatientRoom} entity to a {@link PatientRoomDto}.
     *
     * @param patientRoom The entity to convert.
     * @return The corresponding {@link PatientRoomDto}.
     */
    private PatientRoomDto convertToDto(PatientRoom patientRoom) {
        return PatientRoomDto.builder()
                .id(patientRoom.getId())
                .patientId(patientRoom.getPatient().getId())
                .roomId(patientRoom.getRoom().getId())
                .checkInDate(patientRoom.getCheckInDate())
                .checkOutDate(patientRoom.getCheckOutDate())
                .observations(patientRoom.getObservations())
                .build();
    }

    /**
     * Converts a {@link PatientRoomDto} to a {@link PatientRoom} entity.
     *
     * @param dto The DTO to convert.
     * @return The corresponding {@link PatientRoom} entity.
     */
    private PatientRoom convertToEntity(PatientRoomDto dto) {
        return PatientRoom.builder()
                .id(dto.getId())
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .observations(dto.getObservations())
                .build();
    }
}
