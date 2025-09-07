package com.spring.backend.helper;

import com.spring.backend.utils.DateUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenHelper {

  public static final String AUTHORIZATION = "Authorization";
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String TOKEN_TYPE = "tokenType";
  public static final String ACCESS_TOKEN = "accessToken";
  public static final String REFRESH_TOKEN = "refreshToken";
  public static final String TOKEN_ID = "tokenId";

  @Value("${spring.security.secret-key}")
  private String secretKey;

  private SecretKey getSignInKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(String username, Long tokenId) {
    return Jwts.builder()
        .subject(username)
        .claims(Map.of(TOKEN_TYPE, ACCESS_TOKEN, TOKEN_ID, tokenId))
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(DateUtils.getTime(5))
        .signWith(getSignInKey())
        .compact();
  }

  public String generateRefreshToken(String username) {
    return Jwts.builder()
        .subject(username)
        .claim(TOKEN_TYPE, REFRESH_TOKEN)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(DateUtils.getTime(7))
        .signWith(getSignInKey())
        .compact();
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public String extractTokenType(String token) {
    return extractAllClaims(token).get(TOKEN_TYPE, String.class);
  }

  public Long extractTokenId(String token) {
    return extractAllClaims(token).get(TOKEN_ID, Long.class);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
  }
}
