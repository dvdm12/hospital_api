package com.example.miapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a notification sent to a user.
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
public class Notification {

    /** Unique identifier for the notification. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** User to whom the notification is sent. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /** Title of the notification. */
    @Column(nullable = false, length = 100)
    private String title;
    
    /** Content of the notification. */
    @Column(nullable = false, length = 500)
    private String content;
    
    /** Type of notification. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    /** Status of the notification. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;
    
    /** Date and time when the notification was created. */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /** Date and time when the notification was read. */
    private LocalDateTime readAt;
    
    /** Optional link or action URL related to the notification. */
    @Column(length = 255)
    private String actionUrl;
    
    /** Entity type related to this notification (e.g., APPOINTMENT, PRESCRIPTION). */
    @Enumerated(EnumType.STRING)
    private EntityType entityType;
    
    /** Entity ID related to this notification. */
    private Long entityId;
    
    /**
     * Enum representing notification types.
     */
    public enum NotificationType {
        APPOINTMENT_REMINDER,
        APPOINTMENT_CONFIRMATION,
        APPOINTMENT_CANCELLATION,
        PRESCRIPTION_READY,
        NEW_MESSAGE,
        SYSTEM_ALERT,
        MEDICAL_RECORD_UPDATE,
        TEST_RESULT_AVAILABLE
    }
    
    /**
     * Enum representing notification statuses.
     */
    public enum NotificationStatus {
        UNREAD,
        READ,
        DELETED
    }
    
    /**
     * Enum representing entity types that can be related to notifications.
     */
    public enum EntityType {
        APPOINTMENT,
        PRESCRIPTION,
        MEDICAL_RECORD,
        MESSAGE,
        PATIENT,
        DOCTOR,
        SYSTEM
    }
    
    /**
     * Pre-persist hook to set creation timestamp.
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = NotificationStatus.UNREAD;
        }
    }
    
    /**
     * Marks the notification as read.
     */
    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }
}