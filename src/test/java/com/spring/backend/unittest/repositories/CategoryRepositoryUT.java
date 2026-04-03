package com.spring.backend.unittest.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.repository.CategoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class CategoryRepositoryUT {

  @Autowired private CategoryRepository categoryRepository;
  @Autowired private TestEntityManager entityManager;

  private CategoryEntity active;
  private CategoryEntity inactive;

  @BeforeEach
  void setUp() {
    active = CategoryEntity.builder().name("Active Cat").isActive(true).build();
    inactive = CategoryEntity.builder().name("Inactive Cat").isActive(false).build();
    entityManager.persist(active);
    entityManager.persist(inactive);
    entityManager.flush();
  }

  @Test
  @DisplayName("findByIdAndIsActive should filter appropriately")
  void findByIdAndIsActive_Works() {
    Optional<CategoryEntity> found = categoryRepository.findByIdAndIsActive(active.getId(), true);
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Active Cat");

    found = categoryRepository.findByIdAndIsActive(inactive.getId(), true);
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("search specification should filter by name")
  void search_Works() {
    var spec = CategoryRepository.search("Active");
    var results = categoryRepository.findAll(spec);
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getName()).isEqualTo("Active Cat");
  }
}
