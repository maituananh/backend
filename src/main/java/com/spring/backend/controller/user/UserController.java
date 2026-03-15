package com.spring.backend.controller.user;

import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.dto.user.UserDto;
import com.spring.backend.service.ProductService;
import com.spring.backend.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final ProductService productService;

  @PostMapping
  public UserDto createUser(@RequestBody UserDto userDto) {
    return userService.createUser(userDto);
  }

  @GetMapping
  public List<UserDto> getAllUsers() {
    return userService.getAll();
  }

  @GetMapping("/me")
  public UserDto getMyInfo() {
    return userService.getMyInfo();
  }

  @PutMapping("/me")
  public UserDto updateMyInfo(@RequestBody UserDto userDto) {
    return userService.updateMyInfo(userDto);
  }

  @GetMapping("/{id:\\d+}")
  public UserDto getUserById(@PathVariable Long id) {
    return userService.getByIdCard(id);
  }

  @GetMapping("/search")
  public Page<UserDto> searchUsers(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String cardId,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return userService.searchUser(name, email, phone, cardId, username, page, size);
  }

  @DeleteMapping("/{id:\\d+}")
  public void deleteUserById(@PathVariable Long id) {
    userService.delete(id);
  }

  @PutMapping("/{id:\\d+}")
  public UserDto updateUserById(@PathVariable Long id, @RequestBody UserDto userDto) {
    return userService.updateUser(id, userDto);
  }

  @GetMapping("/{userId}/products")
  public List<ProductResponseDto> getProductsByUserId(@PathVariable Long userId) {
    return productService.getProductsByUserId(userId);
  }

  @PostMapping("/avatar")
  public UserDto uploadAvatar(@RequestParam("file") MultipartFile file) {
    return userService.uploadAvatar(file);
  }
}
