package com.example.miapp.mapper;

import com.example.miapp.dto.prescription.CreatePrescriptionItemRequest;
import com.example.miapp.dto.prescription.CreatePrescriptionRequest;
import com.example.miapp.dto.prescription.PrescriptionDto;
import com.example.miapp.dto.prescription.PrescriptionItemDto;
import com.example.miapp.entity.Prescription;
import com.example.miapp.entity.PrescriptionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper for Prescription entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PrescriptionMapper {

    @Mapping(target = "doctorName", expression = "java(prescription.getDoctor().getFullName())")
    @Mapping(target = "patientName", expression = "java(prescription.getPatient().getFullName())")
    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "medicationItems", source = "medicationItems")
    PrescriptionDto toDto(Prescription prescription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "issueDate", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "medicationItems", ignore = true)
    @Mapping(target = "printed", constant = "false")
    @Mapping(target = "printDate", ignore = true)
    Prescription toEntity(CreatePrescriptionRequest request);

    PrescriptionItemDto toItemDto(PrescriptionItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prescription", ignore = true)
    @Mapping(target = "refillsUsed", constant = "0")
    PrescriptionItem toItemEntity(CreatePrescriptionItemRequest request);

    List<PrescriptionDto> toDtoList(List<Prescription> prescriptions);

    List<PrescriptionItemDto> toItemDtoList(List<PrescriptionItem> items);

    List<PrescriptionItem> toItemEntityList(List<CreatePrescriptionItemRequest> requests);
}