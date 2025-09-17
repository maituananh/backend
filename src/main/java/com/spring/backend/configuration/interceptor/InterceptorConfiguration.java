package com.spring.backend.configuration.interceptor;

import static com.spring.backend.helper.JwtTokenHelper.*;

import com.spring.backend.configuration.user_details.UserDetailsCustom;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.helper.JwtTokenHelper;
import com.spring.backend.repository.TokenRepository;
import com.spring.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class InterceptorConfiguration extends OncePerRequestFilter {

  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final JwtTokenHelper jwtTokenHelper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = request.getHeader(AUTHORIZATION);

    if (StringUtils.isEmpty(token)) {
      doFilter(request, response, filterChain);
      return;
    }

    token = token.replace(TOKEN_PREFIX, "");
    String tokenType = jwtTokenHelper.extractTokenType(token);

    if (!ACCESS_TOKEN.equals(tokenType)) {
      throw new BadCredentialsException("Invalid token");
    }

    tokenRepository
        .findByAccessToken(token)
        .ifPresent(
            tokenEntity -> {
              throw new BadCredentialsException(
                  "Token was expired %s".formatted(tokenEntity.getAccessToken()));
            });

    String username = jwtTokenHelper.extractUsername(token);

    if (StringUtils.isEmpty(username)) {
      return;
    }

    UserEntity userEntity = userRepository.findByUsername(username).orElseThrow();
    UserDetailsCustom userDetailsCustom = toUserDetailsCustom(userEntity);

    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            userDetailsCustom, null, userDetailsCustom.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

    doFilter(request, response, filterChain);
  }

  private UserDetailsCustom toUserDetailsCustom(UserEntity userEntity) {
    return UserDetailsCustom.builder()
        .id(userEntity.getId())
        .username(userEntity.getUsername())
        .password(userEntity.getPassword())
        .build();
  }
}
