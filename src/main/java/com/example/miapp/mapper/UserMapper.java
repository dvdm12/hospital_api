package com.example.miapp.mapper;

import com.example.miapp.dto.user.UserDto;
import com.example.miapp.entity.Role;
import com.example.miapp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for User entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);

    // Custom mapping for roles
    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}