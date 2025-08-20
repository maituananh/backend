package com.spring.backend.controller;

import com.spring.backend.dto.UserDto;
import com.spring.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;


@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/users")
public class ProductUser {
    @Autowired
    private UserService userService;

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/{idCard}")
    public UserDto getUserById(@PathVariable String idCard) {
      //  return userService.getByIdCard(Long.parseLong(idCard));
    }

    @GetMapping("/searchName")
    public List<UserDto> searchUserByName(@RequestParam("name") String name) {
     //   return Collections.singletonList(userService.searchName(name));
    }
    @DeleteMapping("/{idCard}")
    public void deleteUserById(@PathVariable String idCard) {
        userService.delete(Long.parseLong(idCard));
    }
    @PutMapping("/{id}")
    public  UserDto updateUserById(@PathVariable("id") Long id, @RequestBody UserDto userDto) {
        return userService.updateUser(id,userDto);
    }
}
