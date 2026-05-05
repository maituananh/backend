package com.spring.backend.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.spring.backend.domain.enums.ProductStatus;
import com.spring.backend.domain.product.Product;
import com.spring.backend.domain.product.ProductQuantity;
import com.spring.backend.infrastructure.entity.ProductEntity;
import com.spring.backend.infrastructure.mapper.ProductMapper;
import com.spring.backend.infrastructure.repository.ProductJpaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterUT {

  @Mock private ProductJpaRepository jpaRepository;

  @Mock private ProductMapper mapper;

  private ProductRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ProductRepositoryAdapter(jpaRepository, mapper);
  }

  @Test
  void findById_delegatesToJpaRepositoryAndMapsResult() {
    ProductEntity entity = new ProductEntity();
    Product domainProduct =
        Product.reconstitute(
            1L, "Test", 100.0, new ProductQuantity(10, 2), ProductStatus.NEW, true, null, null);

    when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
    when(mapper.toDomain(entity)).thenReturn(domainProduct);

    Optional<Product> result = adapter.findById(1L);

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Test");
  }

  @Test
  void findById_returnsEmptyWhenNotFound() {
    when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

    Optional<Product> result = adapter.findById(99L);

    assertThat(result).isEmpty();
  }
}
