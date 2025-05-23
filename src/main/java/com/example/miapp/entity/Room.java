package com.example.miapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "patientRooms")
@EqualsAndHashCode(exclude = "patientRooms")
public class Room {

    /** Unique identifier for the room. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Room number. */
    @Column(nullable = false, unique = true, length = 10)
    private String number;

    /** Floor where the room is located. */
    @Column(nullable = false, length = 5)
    private String floor;

    /** Type of room (e.g., ICU, General, VIP). */
    @Column(nullable = false, length = 20)
    private String type;

    /** Occupancy status (Available, Occupied). */
    @Column(nullable = false, length = 15)
    private String occupancyStatus;

    /** List of patients who occupied this room. */
    @JsonManagedReference
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<PatientRoom> patientRooms;
}
