package com.spring.backend.repository;

import com.spring.backend.entity.CartEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {

  Optional<CartEntity> findByCustomerId(Long customerId);
}
