package com.spring.backend.controller.user;

import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.service.ProductService;
import com.spring.backend.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
  @Autowired private UserService userService;
  @Autowired private ProductService productService;

  @PostMapping
  public UserDto createUser(@RequestBody UserDto userDto) {
    return userService.createUser(userDto);
  }

  @GetMapping
  public List<UserDto> getAllUsers() {
    return userService.getAll();
  }

  @GetMapping("/id/{id}")
  public UserDto getUserById(@PathVariable("id") Long id) {
    return userService.getByIdCard(id);
  }

  @GetMapping("/me")
  public UserDto getMyInfo() {
    return userService.getMyInfo();
  }

  @GetMapping("/search")
  public List<UserDto> searchUsers(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String cardId,
      @RequestParam(name = "username", required = false) String username) {
    return userService.searchUser(name, email, phone, cardId, username);
  }

  @DeleteMapping("/{id}")
  public void deleteUserById(@PathVariable Long id) {
    userService.delete(id);
  }

  @PutMapping("/{id}")
  public UserDto updateUserById(@PathVariable("id") Long id, @RequestBody UserDto userDto) {
    return userService.updateUser(id, userDto);
  }

  @GetMapping("/{userId}/products")
  public List<ProductResponseDto> getProductsByUserId(@PathVariable Long userId) {
    return productService.getProductsByUserId(userId);
  }
}
