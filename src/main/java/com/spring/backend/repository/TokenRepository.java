package com.spring.backend.repository;

import com.spring.backend.entity.TokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, Long> {

  Optional<TokenEntity> findByAccessToken(String token);

  Optional<TokenEntity> findByRefreshToken(String token);
}
