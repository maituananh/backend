package com.spring.backend.dto;

import com.spring.backend.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String email;
    private String name;
    private int age;
    private String phone;
    private String idCard;

    public UserDto(UserEntity user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.age = user.getAge();
        this.phone = user.getPhone();
        this.idCard = user.getIdCard();
    }
}
