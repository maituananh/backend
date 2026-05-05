package com.spring.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.domain.enums.ProductStatus;
import com.spring.backend.domain.product.ProductRepository;
import com.spring.backend.dto.page.Pagination;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.dto.product.ProductSearchDto;
import com.spring.backend.infrastructure.entity.*;
import com.spring.backend.infrastructure.repository.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ProductServiceUT {

  @Mock private CategoryJpaRepository categoryRepository;
  @Mock private UserJpaRepository userRepository;
  @Mock private ProductRepository productRepository; // domain port — D-06
  @Mock private ProductJpaRepository productJpaRepository; // JPA repo for entity-level operations
  @Mock private ImageJpaRepository imageRepository;
  @Mock private CartItemJpaRepository cartItemRepository;
  @Mock private S3Adapter s3Adapter;

  @InjectMocks private ProductService productService;

  @Test
  @DisplayName("getAll should filter active products and handle images")
  void getAll_Works() {
    ProductEntity p1 = new ProductEntity();
    p1.setIsActived(true);
    ImageEntity img = new ImageEntity();
    img.setFileName("test.jpg");
    p1.setImages(List.of(img));

    when(productJpaRepository.findAll()).thenReturn(List.of(p1));
    when(s3Adapter.getUrl("test.jpg")).thenReturn("http://url");

    List<ProductResponseDto> result = productService.getAll();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getImage()).isEqualTo("http://url");
  }

  @Test
  @DisplayName("createProduct should work correctly")
  void createProduct_Works() {
    ProductRequestDto dto =
        ProductRequestDto.builder()
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(1))
            .imageIds(List.of(1L))
            .customerId(10L)
            .categoryId(20L)
            .stockQty(10)
            .price(100.0)
            .build();

    ImageEntity img = new ImageEntity();
    img.setId(1L);
    img.setFileName("f.jpg");
    UserEntity user = new UserEntity();
    user.setId(10L);
    CategoryEntity category = new CategoryEntity();
    category.setId(20L);
    category.setIsActive(true);

    when(imageRepository.findAllById(anyList())).thenReturn(List.of(img));
    when(userRepository.findById(10L)).thenReturn(Optional.of(user));
    when(categoryRepository.findByIdAndIsActive(20L, true)).thenReturn(Optional.of(category));
    when(productJpaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    productService.createProduct(dto);
    verify(productJpaRepository).save(any());
  }

  @Test
  @DisplayName("deleteById should fail if sold out")
  void deleteById_SoldOut() {
    ProductEntity product = new ProductEntity();
    product.setStatus(ProductStatus.SOLD_OUT);
    when(productJpaRepository.findById(1L)).thenReturn(Optional.of(product));

    assertThatThrownBy(() -> productService.deleteById(1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Cannot delete a sold out product");
  }

  @Test
  @DisplayName("search should return pagination")
  void search_Works() {
    ProductSearchDto dto = new ProductSearchDto();
    dto.setPage(0);
    dto.setSize(10);

    Page<ProductEntity> page =
        new PageImpl<>(List.of(new ProductEntity()), PageRequest.of(0, 10), 1);
    when(productJpaRepository.findAll(any(Specification.class), any(PageRequest.class)))
        .thenReturn(page);

    Pagination<ProductResponseDto> result = productService.search(dto);
    assertThat(result.getData()).hasSize(1);
  }

  @Test
  @DisplayName("liquidationProduct should update status")
  void liquidationProduct_Works() {
    ProductEntity product = new ProductEntity();
    product.setStatus(ProductStatus.NEW);
    product.setImages(new ArrayList<>());
    when(productJpaRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productJpaRepository.save(any())).thenReturn(product);

    productService.liquidationProduct(1L);
    assertThat(product.getStatus()).isEqualTo(ProductStatus.LIQUIDATION);
  }

  @Test
  @DisplayName("updateStatusIsProgress should update status for old NEW products")
  void updateStatusIsProgress_Works() {
    ProductEntity p = new ProductEntity();
    p.setStatus(ProductStatus.NEW);
    when(productJpaRepository.findAll(any(Specification.class))).thenReturn(List.of(p));

    productService.updateStatusIsProgress();
    assertThat(p.getStatus()).isEqualTo(ProductStatus.IN_PROGRESS);
    verify(productJpaRepository).saveAll(any());
  }
}
