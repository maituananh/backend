package com.spring.backend.service.mapper;

import com.spring.backend.dto.chat.ChatCreateAccountRequestDto;
import com.spring.backend.dto.chat.ProfileChatResponseDto;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import com.spring.backend.service.chat.CategoryAI;
import com.spring.backend.service.chat.OCRService;
import com.spring.backend.utils.DateUtils;
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
    if (userDto.getRole() != null) {
      userEntity.setRole(userDto.getRole());
    }
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
        .build();
  }

  public static ProfileChatResponseDto toProfileDto(UserEntity entity) {
    return ProfileChatResponseDto.builder()
        .email(entity.getEmail())
        .name(entity.getName())
        .username(entity.getUsername())
        .age(entity.getAge())
        .phone(entity.getPhone())
        .cardId(entity.getCardId())
        .address(entity.getAddress())
        .gender(entity.getGender())
        .result(String.valueOf(CategoryAI.PROFILE))
        .type(String.valueOf(CategoryAI.PROFILE))
        .build();
  }

  public static UserEntity toEntity(
      OCRService.CitizenIdResponse dto,
      ChatCreateAccountRequestDto chatRequestDto,
      String passwordHash) {
    return UserEntity.builder()
        .name(dto.getFullName())
        .cardId(dto.getIdNumber())
        .address(dto.getPlaceOfOrigin())
        .gender(dto.getGender())
        .isActive(true)
        .age(DateUtils.calculateAge(dto.getDateOfBirth()))
        .role(UserRole.CUSTOMER)
        .username(chatRequestDto.getUsername())
        .email(chatRequestDto.getEmail())
        .phone(chatRequestDto.getPhone())
        .password(passwordHash)
        .build();
  }
}
