package com.spring.backend.helper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenHelper {

  public static final String AUTHORIZATION = "Authorization";
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String TOKEN_TYPE_HINT = "token_type_hint";
  public static final String TOKEN_ID = "jti";

  private final KeyPair keyPair;

  public String generateToken(String username, UUID tokenId) {
    return createToken(username, tokenId, 3600_000, OAuth2TokenType.ACCESS_TOKEN);
  }

  public String generateRefreshToken(String username, UUID tokenId) {
    return createToken(username, tokenId, 9600_000, OAuth2TokenType.REFRESH_TOKEN);
  }

  private String createToken(
      String username, UUID tokenId, long expiresIn, OAuth2TokenType tokenType) {
    return Jwts.builder()
        .subject(username)
        .claims(Map.of(TOKEN_TYPE_HINT, tokenType.getValue(), TOKEN_ID, tokenId))
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiresIn)) // 1h
        .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
        .compact();
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Long extractTokenId(String token) {
    return extractClaims(token).get(TOKEN_ID, Long.class);
  }

  public boolean extractTokenType(String token) {
    return extractClaims(token)
        .get(TOKEN_TYPE_HINT, String.class)
        .equals(OAuth2TokenType.ACCESS_TOKEN.getValue());
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractClaims(token);
    return claimsResolver.apply(claims);
  }

  public Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(keyPair.getPublic())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
