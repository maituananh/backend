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
        .address(userDto.getAddress())
        .gender(userDto.getGender())
        .role(UserRole.CUSTOMER)
        .build();
  }

  public static void toEntity(UserDto userDto, UserEntity userEntity) {
    userEntity.setEmail(userDto.getEmail());
    userEntity.setName(userDto.getName());
    userEntity.setAge(userDto.getAge());
    userEntity.setPhone(userDto.getPhone());
    userEntity.setCardId(userDto.getCardId());
    userEntity.setAddress(userDto.getAddress());
    userEntity.setGender(userDto.getGender());
    userEntity.setRole(userDto.getRole());
    userEntity.setAvatarUrl(userDto.getAvatarUrl());
    userEntity.setCccdImageUrl(userDto.getCccdImageUrl());
  }

  public static UserDto toUserDto(UserEntity entity) {
    return UserDto.builder()
        .id(entity.getId())
        .email(entity.getEmail())
        .name(entity.getName())
        .username(entity.getUsername())
        .age(entity.getAge())
        .phone(entity.getPhone())
        .cardId(entity.getCardId())
        .address(entity.getAddress())
        .gender(entity.getGender())
        .role(entity.getRole())
        .avatarUrl(entity.getAvatarUrl()) // thêm dòng này
        .cccdImageUrl(entity.getCccdImageUrl())
        .build();
  }
}
