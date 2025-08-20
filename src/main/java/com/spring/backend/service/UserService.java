package com.spring.backend.service;

import com.spring.backend.dto.UserDto;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.repository.ProductRepositoryUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private ProductRepositoryUser productRepositoryUser;

    public List<UserDto> getAll() {
        List<UserEntity> userEntity = productRepositoryUser.findAll();

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

        UserEntity saveUser = productRepositoryUser.save(userEntity);

        return new UserDto(saveUser);
    }

    public UserDto getByIdCard(long cardId) {
        UserEntity productUser = productRepositoryUser.fineByIdCard(String.valueOf(cardId));
        return new UserDto(productUser);
    }

    public UserDto searchName(String name) {
        List<UserEntity> userEntities = productRepositoryUser.findByNameLikeIgnoreCase(name);

        List<UserDto> userDto = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            userDto.add(new UserDto(userEntity));
        }
        return (UserDto) userDto;
    }

    public void delete(Long cardId) {
        productRepositoryUser.deleteById(cardId);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(userDto.getEmail());
        userEntity.setName(userDto.getName());
        userEntity.setAge(userDto.getAge());
        userEntity.setPhone(userDto.getPhone());
        userEntity.setCardId(userDto.getCardId());

        UserEntity saveUser = productRepositoryUser.save(userEntity);

        return new UserDto(saveUser);
    }
}
