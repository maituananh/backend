package com.spring.backend.repository;

import com.spring.backend.entity.CardEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<CardEntity, Long> {
  List<CardEntity> findByCustomerId(Long customerId);
}
