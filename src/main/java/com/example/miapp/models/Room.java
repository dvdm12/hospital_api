package com.example.miapp.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Represents a hospital room.
 */
@Entity
@Table(name = "room")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "patientRooms")
public class Room {

    /** Unique identifier for the room. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Room number. */
    private String number;

    /** Floor where the room is located. */
    private String floor;

    /** Type of room (e.g., ICU, General, VIP). */
    private String type;

    /** Occupancy status (Available, Occupied). */
    private String occupancyStatus;

    /** List of patients who occupied this room. */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<PatientRoom> patientRooms;
}
