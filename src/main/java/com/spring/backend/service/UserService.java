package com.spring.backend.service;

import com.spring.backend.dto.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired private UserRepository userRepository;

  public List<UserDto> getAll() {
    List<UserEntity> userEntity = userRepository.findAll();

    List<UserDto> userDto = new ArrayList<>();
    for (UserEntity productEntity : userEntity) {
      userDto.add(new UserDto(productEntity));
    }
    return userDto;
  }

  public UserDto createUser(UserDto userDto) {
    UserEntity userEntity = new UserEntity();
    userEntity.setEmail(userDto.getEmail());
    userEntity.setName(userDto.getName());
    userEntity.setAge(userDto.getAge());
    userEntity.setPhone(userDto.getPhone());
    userEntity.setCardId(userDto.getCardId());

    UserEntity saveUser = userRepository.save(userEntity);

    return new UserDto(saveUser);
  }

  public UserDto getByIdCard(Long id) {
    UserEntity productUser = userRepository.findById(id).get();
    return new UserDto(productUser);
  }

  public List<UserDto> searchName(String name) {
    List<UserEntity> userEntities = userRepository.findByNameLikeIgnoreCase(name);

    List<UserDto> userDto = new ArrayList<>();
    for (UserEntity userEntity : userEntities) {
      userDto.add(new UserDto(userEntity));
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
