package com.spring.backend.service;

import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.mapper.UserMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public List<UserDto> getAll() {
    List<UserEntity> userEntity = userRepository.findAll();

    List<UserDto> userDto = new ArrayList<>();
    for (UserEntity productEntity : userEntity) {
      userDto.add(UserMapper.toUserDto(productEntity));
    }
    return userDto;
  }

  public UserDto createUser(UserDto userDto) {
    UserEntity userEntity = UserMapper.toEntity(userDto);
    UserEntity saveUser = userRepository.save(userEntity);

    return UserMapper.toUserDto(saveUser);
  }

  public UserDto getByIdCard(Long id) {
    UserEntity productUser = userRepository.findById(id).orElseThrow();
    return UserMapper.toUserDto(productUser);
  }

  public UserDto getMyInfo() {
    UserDetailsCustom currentUser =
        (UserDetailsCustom) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    UserEntity productUser = userRepository.findById(currentUser.getId()).get();
    return UserMapper.toUserDto(productUser);
  }

  public List<UserDto> searchUser(
      String name, String email, String phone, String cardId, String username) {
    List<UserEntity> usersEntities = userRepository.findAll();

    if (name != null && !name.isEmpty()) {
      usersEntities =
          usersEntities.stream()
              .filter(
                  u ->
                      u.getName() != null && u.getName().toLowerCase().contains(name.toLowerCase()))
              .toList();
    }

    if (email != null && !email.isEmpty()) {
      usersEntities =
          usersEntities.stream()
              .filter(
                  u ->
                      u.getEmail() != null
                          && u.getEmail().toLowerCase().contains(email.toLowerCase()))
              .toList();
    }

    if (phone != null && !phone.isEmpty()) {
      usersEntities =
          usersEntities.stream()
              .filter(u -> u.getPhone() != null && u.getPhone().contains(phone))
              .toList();
    }

    if (cardId != null && !cardId.isEmpty()) {
      usersEntities =
          usersEntities.stream()
              .filter(
                  u ->
                      u.getCardId() != null
                          && u.getCardId().toLowerCase().contains(cardId.toLowerCase()))
              .toList();
    }

    if (username != null && !username.isEmpty()) {
      usersEntities =
          usersEntities.stream()
              .filter(
                  u ->
                      u.getUsername() != null
                          && u.getUsername().toLowerCase().contains(username.toLowerCase()))
              .toList();
    }

    return usersEntities.stream().map(UserMapper::toUserDto).toList();
  }

  public void delete(Long id) {
    userRepository.deleteById(id);
  }

  public UserDto updateUser(Long id, UserDto userDto) {
    //        UserEntity userEntity = new UserEntity();
    //        userEntity.setEmail(userDto.getEmail());
    //        userEntity.setName(userDto.getName());
    //        userEntity.setAge(userDto.getAge());
    //        userEntity.setPhone(userDto.getPhone());
    //        userEntity.setCardId(userDto.getCardId());
    //
    //        UserEntity saveUser = userRepository.save(userEntity);

    return null;
  }
}
