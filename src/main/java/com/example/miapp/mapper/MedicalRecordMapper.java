package com.example.miapp.mapper;

import com.example.miapp.dto.medicalrecord.CreateMedicalEntryRequest;
import com.example.miapp.dto.medicalrecord.MedicalRecordDto;
import com.example.miapp.dto.medicalrecord.MedicalRecordEntryDto;
import com.example.miapp.entity.MedicalRecord;
import com.example.miapp.entity.MedicalRecordEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper for MedicalRecord entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MedicalRecordMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", expression = "java(medicalRecord.getPatient().getFullName())")
    @Mapping(target = "entries", source = "entries")
    MedicalRecordDto toDto(MedicalRecord medicalRecord);

    @Mapping(target = "doctorName", expression = "java(entry.getDoctor().getFullName())")
    @Mapping(target = "appointmentId", source = "appointment.id")
    MedicalRecordEntryDto toEntryDto(MedicalRecordEntry entry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "entryDate", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    MedicalRecordEntry toEntryEntity(CreateMedicalEntryRequest request);

    List<MedicalRecordDto> toDtoList(List<MedicalRecord> medicalRecords);

    List<MedicalRecordEntryDto> toEntryDtoList(List<MedicalRecordEntry> entries);
}