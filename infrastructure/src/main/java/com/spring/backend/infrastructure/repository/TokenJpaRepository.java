package com.spring.backend.infrastructure.repository;

import com.spring.backend.infrastructure.entity.TokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenJpaRepository extends JpaRepository<TokenEntity, Long> {

  Optional<TokenEntity> findByAccessToken(String token);

  Optional<TokenEntity> findByRefreshToken(String token);
}
