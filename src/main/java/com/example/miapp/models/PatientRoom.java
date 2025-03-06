package com.example.miapp.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * Represents the relation between a patient and a room.
 */
@Entity
@Table(name = "patient_room")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PatientRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private Date checkInDate;
    private Date checkOutDate;
    private String observations;
}
