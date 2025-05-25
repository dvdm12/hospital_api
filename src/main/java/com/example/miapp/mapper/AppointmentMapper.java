package com.example.miapp.mapper;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.dto.appointment.AvailableSlotDto;
import com.example.miapp.dto.appointment.CreateAppointmentRequest;
import com.example.miapp.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalTime;
import java.util.List;

/**
 * Mapper for Appointment entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AppointmentMapper {

    @Mapping(target = "doctorName", expression = "java(appointment.getDoctor().getFullName())")
    @Mapping(target = "patientName", expression = "java(appointment.getPatient().getFullName())")
    @Mapping(target = "durationMinutes", expression = "java(appointment.getDurationMinutes())")
    @Mapping(target = "overdue", expression = "java(appointment.isOverdue())")
    AppointmentDto toDto(Appointment appointment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "status", constant = "SCHEDULED")
    @Mapping(target = "confirmed", constant = "false")
    @Mapping(target = "confirmationDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "prescriptions", ignore = true)
    Appointment toEntity(CreateAppointmentRequest request);

    List<AppointmentDto> toDtoList(List<Appointment> appointments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "confirmed", ignore = true)
    @Mapping(target = "confirmationDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "prescriptions", ignore = true)
    void updateEntityFromRequest(CreateAppointmentRequest request, @MappingTarget Appointment appointment);

    // Mapping for available slots from Object[] (from native query)
    default AvailableSlotDto mapToAvailableSlot(Object[] result) {
        if (result == null || result.length < 2) {
            return null;
        }
        
        AvailableSlotDto slot = new AvailableSlotDto();
        slot.setStartTime((LocalTime) result[0]);
        slot.setEndTime((LocalTime) result[1]);
        slot.setAvailable(true);
        
        return slot;
    }

    default List<AvailableSlotDto> mapToAvailableSlots(List<Object[]> results) {
        return results.stream()
                .map(this::mapToAvailableSlot)
                .toList();
    }
}