package com.spring.backend.infrastructure.repository;

import com.spring.backend.domain.enums.ProductStatus;
import com.spring.backend.infrastructure.entity.ProductEntity;
import com.spring.backend.infrastructure.entity.ProductStatistic;
import jakarta.persistence.LockModeType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Repository
public interface ProductJpaRepository
    extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM ProductEntity p WHERE p.id = :id")
  Optional<ProductEntity> findByIdWithLock(Long id);

  @Query(
      """
    SELECT
      MONTH(p.startDate) AS month,
      COUNT(p) AS productCount
    FROM ProductEntity p
    WHERE YEAR(p.startDate) = :year AND p.isActived = true
    GROUP BY MONTH(p.startDate)
    ORDER BY MONTH(p.startDate)
  """)
  List<ProductStatistic> statisticProductByMonth(int year);

  List<ProductEntity> findByCustomerIdAndIsActivedTrue(Long customerId);

  Page<ProductEntity> findByCategoryIdAndStatusAndIdNotAndIsActivedTrueAndAvailableQtyGreaterThan(
      Long categoryId, ProductStatus status, Long id, int availableQty, Pageable pageable);

  static Specification<ProductEntity> findByDateAndStatus(
      final String dateType, final LocalDate localDate, final ProductStatus status) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (localDate != null) {
        predicates.add(cb.lessThan(root.get(dateType), localDate));
      }

      if (status != null) {
        predicates.add(cb.equal(root.get("status"), status));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  static Specification<ProductEntity> search(
      String name,
      ProductStatus status,
      Double price,
      LocalDate startDate,
      LocalDate endDate,
      String code,
      Long customerId,
      List<Integer> categoryIds) {

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (StringUtils.isNotBlank(name)) {
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

      if (StringUtils.isNotBlank(code)) {
        predicates.add(cb.equal(root.get("code"), code));
      }

      if (!CollectionUtils.isEmpty(categoryIds)) {
        predicates.add(root.get("category").get("id").in(categoryIds));
      }

      if (customerId != null) {
        predicates.add(cb.equal(root.get("customer").get("id"), customerId));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
