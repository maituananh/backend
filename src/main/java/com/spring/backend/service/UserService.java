package com.spring.backend.service;

import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.mapper.UserMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

  @Transactional
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

  public Page<UserDto> searchUser(
      String name, String email, String phone, String cardId, String username, int page, int size) {
    Specification<UserEntity> spec = UserRepository.search(name, email, phone, cardId, username);
    Pageable pageable = PageRequest.of(page, size);

    Page<UserEntity> usersEntities = userRepository.findAll(spec, pageable);

    List<UserDto> userDtos =
        usersEntities.getContent().stream().map(UserMapper::toUserDto).toList();

    return new PageImpl<>(userDtos, pageable, usersEntities.getTotalElements());
  }

  @Transactional
  public void delete(Long id) {
    userRepository.deleteById(id);
  }

  @Transactional
  public UserDto updateUser(Long id, UserDto userDto) {
    UserEntity userEntity =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    UserMapper.toEntity(userDto, userEntity);
    UserEntity updatedUser = userRepository.save(userEntity);

    return UserMapper.toUserDto(updatedUser);
  }
}
