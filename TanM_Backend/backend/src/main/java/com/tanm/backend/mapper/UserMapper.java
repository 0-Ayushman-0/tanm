package com.tanm.backend.mapper;

import com.tanm.backend.dto.RegisterRequest;
import com.tanm.backend.dto.UserDto;
import com.tanm.backend.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(AppUser user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public AppUser toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }
        return AppUser.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }
}
