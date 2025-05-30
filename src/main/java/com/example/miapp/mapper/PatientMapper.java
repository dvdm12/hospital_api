package com.example.miapp.mapper;

import com.example.miapp.dto.patient.CreatePatientRequest;
import com.example.miapp.dto.patient.PatientDto;
import com.example.miapp.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper for Patient entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PatientMapper {

    @Mapping(target = "fullName", expression = "java(patient.getFullName())")
    @Mapping(target = "age", expression = "java(patient.getAge())")
    PatientDto toDto(Patient patient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "prescriptions", ignore = true)
    Patient toEntity(CreatePatientRequest request);

    List<PatientDto> toDtoList(List<Patient> patients);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "prescriptions", ignore = true)
    void updateEntityFromRequest(CreatePatientRequest request, @MappingTarget Patient patient);

    // Custom mapping to extract user account fields from CreatePatientRequest
    default String extractUsername(CreatePatientRequest request) {
        return request.getUsername();
    }

    default String extractEmail(CreatePatientRequest request) {
        return request.getEmail();
    }

    default String extractPassword(CreatePatientRequest request) {
        return request.getPassword();
    }
    
    // Nuevo método para extraer el número de cédula
    default String extractCc(CreatePatientRequest request) {
        return request.getCc();
    }
}