package com.spring.backend.infrastructure.repository;

import com.spring.backend.infrastructure.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageJpaRepository extends JpaRepository<ImageEntity, Long> {}
