package com.spring.backend.service;

import com.spring.backend.dto.auth.AuthRequestDto;
import com.spring.backend.dto.auth.AuthResponseDto;
import com.spring.backend.dto.auth.LogoutRequestDto;
import com.spring.backend.entity.TokenEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.helper.JwtTokenHelper;
import com.spring.backend.repository.TokenRepository;
import com.spring.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final TokenRepository tokenRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenHelper jwtTokenHelper;
  private final UserRepository userRepository;

  public AuthResponseDto createToken(AuthRequestDto authRequestDto) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            authRequestDto.getUsername(), authRequestDto.getPassword()));

    return generateToken(authRequestDto.getUsername());
  }

  public void handleLogout(String token) {
    String accessToken = token.replace(JwtTokenHelper.TOKEN_PREFIX, "");
    Long tokenId = jwtTokenHelper.extractTokenId(accessToken);

    tokenRepository
        .findById(tokenId)
        .ifPresent(
            tokenEntity -> {
              tokenEntity.setAccessToken(accessToken);
              tokenRepository.save(tokenEntity);
            });
  }

  public AuthResponseDto handleRenewToken(LogoutRequestDto requestDto) throws BadRequestException {
    String refreshToken = requestDto.getAccessToken().replace(JwtTokenHelper.TOKEN_PREFIX, "");
    TokenEntity tokenEntity = tokenRepository.findByRefreshToken(refreshToken).orElseThrow();

    if (StringUtils.isNoneBlank(tokenEntity.getAccessToken())) {
      throw new BadRequestException("Token has been renewal");
    }

    String username = jwtTokenHelper.extractUsername(refreshToken);

    UserEntity userEntity = userRepository.findByUsername(username).orElseThrow();

    return generateToken(userEntity.getUsername());
  }

  private AuthResponseDto generateToken(String username) {
    String refreshToken = jwtTokenHelper.generateRefreshToken(username);

    TokenEntity tokenEntity =
        tokenRepository.save(TokenEntity.builder().refreshToken(refreshToken).build());

    String accessToken = jwtTokenHelper.generateToken(username, tokenEntity.getId());

    return AuthResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }
}
