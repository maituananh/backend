package com.spring.backend.repository;

import com.spring.backend.entity.CardEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {

  Optional<CardEntity> findByCustomerId(Long customerId);
}
