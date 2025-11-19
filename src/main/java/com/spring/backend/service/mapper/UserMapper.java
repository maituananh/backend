package com.spring.backend.service.mapper;

import com.spring.backend.dto.user.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

  public static UserEntity toEntity(UserDto userDto) {
    return UserEntity.builder()
        .email(userDto.getEmail())
        .name(userDto.getName())
        .age(userDto.getAge())
        .phone(userDto.getPhone())
        .cardId(userDto.getCardId())
        .role(UserRole.CUSTOMER)
        .build();
  }

  public static UserDto toUserDto(UserEntity entity) {
    return UserDto.builder()
        .email(entity.getEmail())
        .name(entity.getName())
        .username(entity.getUsername())
        .age(entity.getAge())
        .phone(entity.getPhone())
        .cardId(entity.getCardId())
        .role(entity.getRole())
        .build();
  }
}
