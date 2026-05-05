package com.spring.backend.infrastructure.repository;

import com.spring.backend.infrastructure.entity.CartEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartJpaRepository extends JpaRepository<CartEntity, Long> {

  Optional<CartEntity> findByCustomerId(Long customerId);
}
