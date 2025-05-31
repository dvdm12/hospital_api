package com.example.miapp.mapper;

import com.example.miapp.config.MapStructConfig;
import com.example.miapp.dto.specialty.SpecialtyDto;
import com.example.miapp.entity.Specialty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper for Specialty entity and DTOs
 */
@Mapper(config = MapStructConfig.class)
public interface SpecialtyMapper {

    @Mapping(target = "doctorCount", expression = "java(specialty.getDoctorSpecialties() != null ? specialty.getDoctorSpecialties().size() : 0)")
    SpecialtyDto toDto(Specialty specialty);

    List<SpecialtyDto> toDtoList(List<Specialty> specialties);
}