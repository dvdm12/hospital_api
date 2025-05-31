package com.example.miapp.mapper;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper manual para entidades Appointment
 */
@Component
public class ManualAppointmentMapper {

    /**
     * Convierte una entidad Appointment a un DTO
     */
    public AppointmentDto toDto(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        
        AppointmentDto dto = new AppointmentDto();
        dto.setId(appointment.getId());
        dto.setDate(appointment.getDate());
        dto.setEndTime(appointment.getEndTime());
        
        if (appointment.getDoctor() != null) {
            dto.setDoctorName(appointment.getDoctor().getFullName());
        }
        
        if (appointment.getPatient() != null) {
            dto.setPatientName(appointment.getPatient().getFullName());
        }
        
        dto.setReason(appointment.getReason());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setConfirmed(appointment.isConfirmed());
        dto.setLocation(appointment.getLocation());
        
        return dto;
    }
    
    /**
     * Convierte una lista de entidades a DTOs
     */
    public List<AppointmentDto> toDtoList(List<Appointment> appointments) {
        if (appointments == null) {
            return new ArrayList<>();
        }
        
        return appointments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte una página de entidades a una página de DTOs
     */
    public Page<AppointmentDto> toDtoPage(Page<Appointment> page) {
        if (page == null) {
            return Page.empty();
        }
        
        List<AppointmentDto> dtos = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }
}