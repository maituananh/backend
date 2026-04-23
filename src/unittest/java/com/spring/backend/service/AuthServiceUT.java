package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.dto.auth.AuthRequestDto;
import com.spring.backend.dto.auth.AuthResponseDto;
import com.spring.backend.dto.auth.RenewTokenRequestDto;
import com.spring.backend.entity.TokenEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.helper.JwtTokenHelper;
import com.spring.backend.repository.TokenRepository;
import com.spring.backend.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

@ExtendWith(MockitoExtension.class)
class AuthServiceUT {

  @Mock private TokenRepository tokenRepository;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtTokenHelper jwtTokenHelper;
  @Mock private UserRepository userRepository;

  @InjectMocks private AuthService authService;

  @Test
  @DisplayName("createToken should return response")
  void createToken_Works() {
    AuthRequestDto request = AuthRequestDto.builder().username("user").password("pass").build();

    when(jwtTokenHelper.generateRefreshToken("user")).thenReturn("refresh");
    when(tokenRepository.save(any()))
        .thenAnswer(
            i -> {
              TokenEntity t = i.getArgument(0);
              t.setId(10L);
              return t;
            });
    when(jwtTokenHelper.generateToken("user", 10L)).thenReturn("access");

    AuthResponseDto response = authService.createToken(request);

    assertThat(response.getAccessToken()).isEqualTo("access");
    assertThat(response.getRefreshToken()).isEqualTo("refresh");
    verify(authenticationManager).authenticate(any());
  }

  @Test
  @DisplayName("handleLogout should save accessToken to blocklist")
  void handleLogout_Works() {
    String rawToken = "Bearer access_token";
    when(jwtTokenHelper.extractTokenId("access_token")).thenReturn(1L);
    TokenEntity tokenEntity = new TokenEntity();
    when(tokenRepository.findById(1L)).thenReturn(Optional.of(tokenEntity));

    authService.handleLogout(rawToken);

    assertThat(tokenEntity.getAccessToken()).isEqualTo("access_token");
    verify(tokenRepository).save(tokenEntity);
  }

  @Test
  @DisplayName("handleRenewToken should work correctly")
  void handleRenewToken_Works() throws BadRequestException {
    RenewTokenRequestDto request =
        RenewTokenRequestDto.builder().refreshToken("Bearer refresh_token").build();

    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setId(1L);
    tokenEntity.setRefreshToken("refresh_token");

    UserEntity user = new UserEntity();
    user.setUsername("user");

    when(tokenRepository.findByRefreshToken("refresh_token")).thenReturn(Optional.of(tokenEntity));
    when(jwtTokenHelper.extractUsername("refresh_token")).thenReturn("user");
    when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
    when(jwtTokenHelper.generateRefreshToken("user")).thenReturn("new_refresh");
    when(tokenRepository.save(any()))
        .thenAnswer(
            i -> {
              TokenEntity t = i.getArgument(0);
              t.setId(2L);
              return t;
            });
    when(jwtTokenHelper.generateToken("user", 2L)).thenReturn("new_access");

    AuthResponseDto response = authService.handleRenewToken(request);

    assertThat(response.getAccessToken()).isEqualTo("new_access");
  }

  @Test
  @DisplayName("handleRenewToken should fail if token already used")
  void handleRenewToken_AlreadyUsed() {
    RenewTokenRequestDto request =
        RenewTokenRequestDto.builder().refreshToken("Bearer refresh").build();

    TokenEntity tokenEntity = new TokenEntity();
    tokenEntity.setAccessToken("old_access");

    when(tokenRepository.findByRefreshToken("refresh")).thenReturn(Optional.of(tokenEntity));

    assertThatThrownBy(() -> authService.handleRenewToken(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Token has been renewal");
  }

  @Test
  @DisplayName("handleRenewToken should fail if token not found")
  void handleRenewToken_TokenNotFound() {
    RenewTokenRequestDto request =
        RenewTokenRequestDto.builder().refreshToken("Bearer unknown").build();

    when(tokenRepository.findByRefreshToken("unknown")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.handleRenewToken(request))
        .isInstanceOf(NoSuchElementException.class);
  }
}
