package com.spring.backend.repository;

import com.spring.backend.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

  @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', ?1, '%'))")
  Page<ProductEntity> findByNameLikeIgnoreCase(String name, Pageable pageable);

  Page<ProductEntity> findByType(
      String type,
      Pageable pageable); // lấy danh sách sản phẩm theo loại , có phân trang và sắp xếp
}
