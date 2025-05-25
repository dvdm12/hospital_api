package com.example.miapp.mapper;

import com.example.miapp.dto.dashboard.AppointmentStatusStatsDto;
import com.example.miapp.dto.dashboard.DoctorStatsDto;
import com.example.miapp.dto.dashboard.SpecialtyStatsDto;
import com.example.miapp.entity.Appointment.AppointmentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for Dashboard statistics DTOs
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DashboardMapper {

    // Mapping for appointment status statistics from Object[]
    default AppointmentStatusStatsDto mapToAppointmentStatusStats(Object[] result, long totalAppointments) {
        if (result == null || result.length < 2) {
            return null;
        }
        
        AppointmentStatusStatsDto stats = new AppointmentStatusStatsDto();
        stats.setStatus((AppointmentStatus) result[0]);
        stats.setCount(((Number) result[1]).longValue());
        stats.setPercentage(totalAppointments > 0 ? (stats.getCount() * 100.0) / totalAppointments : 0.0);
        
        return stats;
    }

    default List<AppointmentStatusStatsDto> mapToAppointmentStatusStatsList(List<Object[]> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        
        long totalAppointments = results.stream()
                .mapToLong(result -> ((Number) result[1]).longValue())
                .sum();
        
        return results.stream()
                .map(result -> mapToAppointmentStatusStats(result, totalAppointments))
                .collect(Collectors.toList());
    }

    // Mapping for specialty statistics from Object[]
    default SpecialtyStatsDto mapToSpecialtyStats(Object[] result) {
        if (result == null || result.length < 3) {
            return null;
        }
        
        SpecialtyStatsDto stats = new SpecialtyStatsDto();
        stats.setSpecialtyId(((Number) result[0]).longValue());
        stats.setSpecialtyName((String) result[1]);
        stats.setDoctorCount(((Number) result[2]).longValue());
        
        return stats;
    }

    default List<SpecialtyStatsDto> mapToSpecialtyStatsList(List<Object[]> results) {
        return results.stream()
                .map(this::mapToSpecialtyStats)
                .collect(Collectors.toList());
    }

    // Mapping for doctor statistics from Object[]
    default DoctorStatsDto mapToDoctorStats(Object[] result) {
        if (result == null || result.length < 4) {
            return null;
        }
        
        DoctorStatsDto stats = new DoctorStatsDto();
        stats.setDoctorId(((Number) result[0]).longValue());
        stats.setDoctorName(result[1] + " " + result[2]); // firstName + lastName
        stats.setAppointmentCount(((Number) result[3]).longValue());
        
        return stats;
    }

    default List<DoctorStatsDto> mapToDoctorStatsList(List<Object[]> results) {
        return results.stream()
                .map(this::mapToDoctorStats)
                .collect(Collectors.toList());
    }

    // Mapping for hour/day statistics from Object[]
    default Map<String, Integer> mapToHourlyStats(List<Object[]> results) {
        return results.stream()
                .collect(Collectors.toMap(
                    result -> String.valueOf(result[0]), // hour or day
                    result -> ((Number) result[1]).intValue() // count
                ));
    }
}