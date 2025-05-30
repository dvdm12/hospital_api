package com.example.miapp.mapper;

import com.example.miapp.dto.doctor.DoctorDto;
import com.example.miapp.dto.doctor.DoctorScheduleDto;
import com.example.miapp.dto.doctor.CreateDoctorRequest;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSchedule;
import com.example.miapp.entity.DoctorSpecialty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for Doctor entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DoctorMapper {

    @Mapping(target = "fullName", expression = "java(doctor.getFullName())")
    @Mapping(target = "specialties", expression = "java(mapSpecialties(doctor.getDoctorSpecialties()))")
    @Mapping(target = "workSchedules", source = "workSchedules")
    DoctorDto toDto(Doctor doctor);

    List<DoctorDto> toDtoList(List<Doctor> doctors);

    DoctorScheduleDto toScheduleDto(DoctorSchedule schedule);

    List<DoctorScheduleDto> toScheduleDtoList(Set<DoctorSchedule> schedules);
    
    // Método para mapear CreateDoctorRequest a Doctor (similar al de PatientMapper)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "doctorSpecialties", ignore = true)
    @Mapping(target = "prescriptions", ignore = true)
    @Mapping(target = "workSchedules", ignore = true)
    Doctor toEntity(CreateDoctorRequest request);
    
    // Método para actualizar un Doctor existente desde CreateDoctorRequest
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "doctorSpecialties", ignore = true)
    @Mapping(target = "prescriptions", ignore = true)
    @Mapping(target = "workSchedules", ignore = true)
    void updateEntityFromRequest(CreateDoctorRequest request, @MappingTarget Doctor doctor);
    
    // Custom mapping para extraer campos de usuario
    default String extractUsername(CreateDoctorRequest request) {
        return request.getUsername();
    }

    default String extractEmail(CreateDoctorRequest request) {
        return request.getEmail();
    }

    default String extractPassword(CreateDoctorRequest request) {
        return request.getPassword();
    }
    
    // Nuevo método para extraer el número de cédula
    default String extractCc(CreateDoctorRequest request) {
        return request.getCc();
    }

    // Custom mapping for specialties
    default List<String> mapSpecialties(List<DoctorSpecialty> doctorSpecialties) {
        if (doctorSpecialties == null) {
            return List.of();
        }
        
        return doctorSpecialties.stream()
                .map(ds -> ds.getSpecialty().getName())
                .collect(Collectors.toList());
    }
}