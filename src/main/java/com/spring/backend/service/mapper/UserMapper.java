package com.spring.backend.service.mapper;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.chat.ProfileResultDto;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.UserRole;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

  public static UserEntity toEntity(UserDto userDto) {
    return UserEntity.builder()
        .username(userDto.getUsername())
        .password("123456")
        .email(userDto.getEmail())
        .name(userDto.getName())
        .age(userDto.getAge())
        .birthDate(
            userDto.getBirthDate() != null
                ? LocalDate.parse(userDto.getBirthDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : null)
        .phone(userDto.getPhone())
        .cardId(userDto.getCardId() != null ? userDto.getCardId() : "")
        .address(userDto.getAddress())
        .gender(userDto.getGender())
        .avatar(userDto.getAvatar())
        .role(UserRole.CUSTOMER)
        .isActive(true)
        .build();
  }

  public static void toEntity(UserDto userDto, UserEntity userEntity) {
    userEntity.setEmail(userDto.getEmail());
    userEntity.setName(userDto.getName());
    userEntity.setAge(userDto.getAge());
    if (userDto.getBirthDate() != null) {
      userEntity.setBirthDate(
          LocalDate.parse(userDto.getBirthDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }
    userEntity.setPhone(userDto.getPhone());
    if (userDto.getCardId() != null) {
      userEntity.setCardId(userDto.getCardId());
    }
    userEntity.setAddress(userDto.getAddress());
    userEntity.setGender(userDto.getGender());
    userEntity.setAvatar(userDto.getAvatar());
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
        .birthDate(
            entity.getBirthDate() != null
                ? entity.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : null)
        .phone(entity.getPhone())
        .cardId(entity.getCardId())
        .address(entity.getAddress())
        .gender(entity.getGender())
        .role(entity.getRole())
        .avatar(entity.getAvatar())
        .build();
  }

  public static UserDto toUserDto(UserEntity entity, S3Adapter s3Adapter) {
    UserDto dto = toUserDto(entity);
    if (entity.getAvatar() != null && s3Adapter != null) {
      dto.setAvatar(s3Adapter.getUrl(entity.getAvatar()));
    }
    return dto;
  }

  public static ProfileResultDto toProfileDto(UserEntity entity) {
    return ProfileResultDto.builder()
        .email(entity.getEmail())
        .name(entity.getName())
        .username(entity.getUsername())
        .age(entity.getAge())
        .birthDate(
            entity.getBirthDate() != null
                ? entity.getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : null)
        .phone(entity.getPhone())
        .cardId(entity.getCardId())
        .address(entity.getAddress())
        .gender(entity.getGender())
        .avatar(entity.getAvatar())
        .build();
  }

  public static ProfileResultDto toProfileDto(UserEntity entity, S3Adapter s3Adapter) {
    ProfileResultDto dto = toProfileDto(entity);
    if (entity.getAvatar() != null && s3Adapter != null) {
      dto.setAvatar(s3Adapter.getUrl(entity.getAvatar()));
    }
    return dto;
  }
}
