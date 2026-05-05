package com.spring.backend.infrastructure.repository;

import com.spring.backend.infrastructure.entity.CardEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardJpaRepository extends JpaRepository<CardEntity, Long> {
  List<CardEntity> findByCustomerId(Long customerId);
}
