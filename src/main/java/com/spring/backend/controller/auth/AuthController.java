package com.spring.backend.controller.auth;

import com.spring.backend.dto.auth.AuthRequestDto;
import com.spring.backend.dto.auth.AuthResponseDto;
import com.spring.backend.dto.auth.LogoutRequestDto;
import com.spring.backend.helper.JwtTokenHelper;
import com.spring.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/token")
  public AuthResponseDto getToken(@RequestBody AuthRequestDto authRequestDto) {
    return authService.createToken(authRequestDto);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@RequestHeader(JwtTokenHelper.AUTHORIZATION) String token) {
    authService.handleLogout(token);
  }

  @PostMapping("/refresh-token")
  public AuthResponseDto renewToken(@RequestBody LogoutRequestDto requestDto)
      throws BadRequestException {
    return authService.handleRenewToken(requestDto);
  }
}
