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

  public List<UserDto> searchName(String name) {
    List<UserEntity> userEntities = userRepository.findByNameLikeIgnoreCase(name);

    List<UserDto> userDto = new ArrayList<>();
    for (UserEntity userEntity : userEntities) {
      userDto.add(UserMapper.toUserDto(userEntity));
    }

    return userDto;
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
