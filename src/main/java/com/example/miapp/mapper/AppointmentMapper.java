package com.example.miapp.mapper;

import com.example.miapp.dto.appointment.AppointmentDto;
import com.example.miapp.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Mapper para la entidad Appointment y sus DTOs
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AppointmentMapper {

    /**
     * Convierte una entidad Appointment a un DTO
     */
    @Mapping(target = "doctorName", expression = "java(appointment.getDoctor() != null ? appointment.getDoctor().getFullName() : null)")
    @Mapping(target = "patientName", expression = "java(appointment.getPatient() != null ? appointment.getPatient().getFullName() : null)")
    AppointmentDto toDto(Appointment appointment);

    /**
     * Convierte una lista de entidades Appointment a una lista de DTOs
     */
    List<AppointmentDto> toDtoList(List<Appointment> appointments);

    /**
     * Método default para mapear una página de entidades a una página de DTOs
     */
    default Page<AppointmentDto> toDtoPage(Page<Appointment> appointmentPage) {
        return appointmentPage.map(this::toDto);
    }
}