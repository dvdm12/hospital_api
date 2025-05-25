package com.example.miapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents system and user settings.
 */
@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "user")
public class Settings {

    /** Unique identifier for the settings. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** User associated with these settings (null for system settings). */
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    /** Setting key. */
    @Column(nullable = false, length = 50)
    private String key;
    
    /** Setting value. */
    @Column(nullable = false, length = 500)
    private String value;
    
    /** Setting group or category. */
    @Column(length = 50)
    private String group;
    
    /** Description of the setting. */
    @Column(length = 255)
    private String description;
    
    /** Data type of the setting. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataType dataType;
    
    /** Date and time when the setting was last updated. */
    private LocalDateTime updatedAt;
    
    /** Flag to indicate if this is a system setting. */
    @Builder.Default
    private boolean systemSetting = false;
    
    /** Flag to indicate if this setting is visible to users. */
    @Builder.Default
    private boolean visible = true;
    
    /** Flag to indicate if this setting is editable by users. */
    @Builder.Default
    private boolean editable = true;
    
    /**
     * Enum representing data types for settings.
     */
    public enum DataType {
        STRING,
        INTEGER,
        BOOLEAN,
        FLOAT,
        JSON,
        DATE,
        TIME,
        DATETIME,
        COLOR
    }
    
    /**
     * Pre-update hook to set update timestamp.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Converts the value to a boolean.
     * 
     * @return The boolean value
     */
    public boolean getBooleanValue() {
        return Boolean.parseBoolean(this.value);
    }
    
    /**
     * Converts the value to an integer.
     * 
     * @return The integer value
     */
    public Integer getIntegerValue() {
        try {
            return Integer.parseInt(this.value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Converts the value to a double.
     * 
     * @return The double value
     */
    public Double getDoubleValue() {
        try {
            return Double.parseDouble(this.value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}