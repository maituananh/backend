package com.spring.backend.controller;

import com.spring.backend.dto.UserDto;
import com.spring.backend.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/users")
public class UserController {
  @Autowired private UserService userService;

  @PostMapping
  public UserDto createUser(@RequestBody UserDto userDto) {
    return userService.createUser(userDto);
  }

  @GetMapping
  public List<UserDto> getAllUsers() {
    return userService.getAll();
  }

  @GetMapping("/{id}")
  public UserDto getUserById(@PathVariable("id") Long id) {
    return userService.getByIdCard(id);
  }

  @GetMapping("/searchName")
  public List<UserDto> searchUserByName(@RequestParam("name") String name) {
    return userService.searchName(name);
  }

  @DeleteMapping("/{id}")
  public void deleteUserById(@PathVariable Long id) {
    userService.delete(id);
  }

  @PutMapping("/{id}")
  public UserDto updateUserById(@PathVariable("id") Long id, @RequestBody UserDto userDto) {
    return userService.updateUser(id, userDto);
  }
}
