package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.ProductStatus;
import com.spring.backend.enums.UserRole;
import com.spring.backend.repository.ProductRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ProductRepositoryUT {

  @Autowired private ProductRepository productRepository;
  @Autowired private TestEntityManager entityManager;

  private ProductEntity product;
  private UserEntity user;
  private CategoryEntity category;

  @BeforeEach
  void setUp() {
    user =
        UserEntity.builder()
            .username("u")
            .password("p")
            .email("e@e.com")
            .phone("1")
            .cardId("1")
            .role(UserRole.CUSTOMER)
            .build();
    category = CategoryEntity.builder().name("C").isActive(true).build();
    entityManager.persist(user);
    entityManager.persist(category);

    product =
        ProductEntity.builder()
            .name("Sample Product")
            .price(100.0)
            .status(ProductStatus.NEW)
            .startDate(LocalDate.now().minusDays(1))
            .endDate(LocalDate.now().plusDays(1))
            .isActived(true)
            .availableQty(10)
            .customer(user)
            .category(category)
            .build();
    entityManager.persist(product);
    entityManager.flush();
  }

  @Test
  @DisplayName("search specification should filter complexes")
  void search_Works() {
    Specification<ProductEntity> spec =
        ProductRepository.search("Sample", ProductStatus.NEW, 100.0, null, null, null, null, null);
    List<ProductEntity> result = productRepository.findAll(spec);
    assertThat(result).hasSize(1);

    spec =
        ProductRepository.search(
            "Sample", ProductStatus.SOLD_OUT, null, null, null, null, null, null);
    result = productRepository.findAll(spec);
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("findByDateAndStatus should filter for scheduled tasks")
  void findByDateAndStatus_Works() {
    Specification<ProductEntity> spec =
        ProductRepository.findByDateAndStatus("startDate", LocalDate.now(), ProductStatus.NEW);
    List<ProductEntity> result = productRepository.findAll(spec);
    assertThat(result).hasSize(1);
  }
}
