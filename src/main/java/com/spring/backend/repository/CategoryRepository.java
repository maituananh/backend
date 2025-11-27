package com.spring.backend.repository;

import com.spring.backend.entity.CategoryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

  List<CategoryEntity> findByIsActiveIsTrue();

  Optional<CategoryEntity> findByNameIgnoreCase(String name);
}
