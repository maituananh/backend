package com.spring.backend.infrastructure.repository;

import com.spring.backend.infrastructure.entity.CategoryEntity;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryJpaRepository
    extends JpaRepository<CategoryEntity, Long>, JpaSpecificationExecutor<CategoryEntity> {

  static Specification<CategoryEntity> search(String name) {
    return (root, query, cb) -> {
      if (name == null || name.isEmpty()) return cb.conjunction();
      return cb.like(root.get("name"), "%" + name + "%");
    };
  }

  Optional<CategoryEntity> findByIdAndIsActive(Long id, Boolean isActive);

  Optional<CategoryEntity> findByIdAndIsActiveIsTrue(Long id);
}
