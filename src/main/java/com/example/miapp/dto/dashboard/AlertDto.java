package com.example.miapp.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for system alerts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {
    private String type;
    private String message;
    private String severity;
    private LocalDateTime timestamp;
    private String action;
}