package com.example.miapp.mapper;

import com.example.miapp.dto.doctor.DoctorDto;
import com.example.miapp.dto.doctor.DoctorScheduleDto;
import com.example.miapp.entity.Doctor;
import com.example.miapp.entity.DoctorSchedule;
import com.example.miapp.entity.DoctorSpecialty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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