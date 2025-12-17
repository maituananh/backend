package com.spring.backend.repository;

import com.spring.backend.entity.ProductEntity;
import com.spring.backend.enums.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository
    extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

  @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', ?1, '%'))")
  Page<ProductEntity> findByNameLikeIgnoreCase(String name, Pageable pageable);

  Page<ProductEntity> findByType(
      String type,
      Pageable pageable); // lấy danh sách sản phẩm theo loại , có phân trang và sắp xếp

  List<ProductEntity> findByCustomerId(Long customerId);

  static Specification<ProductEntity> search(
      String name,
      ProductStatus status,
      Double price,
      LocalDate startDate,
      LocalDate endDate,
      String code) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (name != null) {
        predicates.add(cb.like(root.get("name"), "%" + name + "%"));
      }

      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }

      if (price != null) {
        predicates.add(cb.equal(root.get("price"), price));
      }

      if (startDate != null && endDate != null) {
        predicates.add(cb.between(root.get("startDate"), startDate, endDate));
      }

      if (code != null && code.isEmpty()) {
        predicates.add(cb.equal(root.get("code"), code));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
