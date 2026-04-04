package com.spring.backend.unittest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.page.Pagination;
import com.spring.backend.dto.product.ProductDetailResponseDto;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.dto.product.ProductSearchDto;
import com.spring.backend.entity.*;
import com.spring.backend.enums.ProductStatus;
import com.spring.backend.repository.*;
import com.spring.backend.service.ProductService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ProductServiceUT {

  @Mock private CategoryRepository categoryRepository;
  @Mock private UserRepository userRepository;
  @Mock private ProductRepository productRepository;
  @Mock private ImageRepository imageRepository;
  @Mock private CartItemRepository cartItemRepository;
  @Mock private S3Adapter s3Adapter;
  @InjectMocks private ProductService productService;

  private UserEntity customer;
  private CategoryEntity category;
  private ProductEntity product;
  private ImageEntity image;
  private ProductRequestDto productRequestDto;

  @BeforeEach
  void setUp() {
    customer = UserEntity.builder().id(1L).username("customer").build();
    category = CategoryEntity.builder().id(10L).name("Category").isActive(true).build();
    image = ImageEntity.builder().id(100L).fileName("prod.jpg").build();
    product =
        ProductEntity.builder()
            .id(1000L)
            .name("Product 1")
            .price(100.0)
            .isActived(true)
            .status(ProductStatus.NEW)
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().plusDays(5))
            .images(new ArrayList<>(List.of(image)))
            .category(category)
            .customer(customer)
            .build();

    productRequestDto = new ProductRequestDto();
    productRequestDto.setName("New Product");
    productRequestDto.setPrice(150.0);
    productRequestDto.setStartDate(LocalDate.now().plusDays(1));
    productRequestDto.setEndDate(LocalDate.now().plusDays(5));
    productRequestDto.setStockQty(10);
    productRequestDto.setCategoryId(10L);
    productRequestDto.setCustomerId(1L);
    productRequestDto.setImageIds(List.of(100L, 101L, 102L, 103L));
  }

  @Nested
  @DisplayName("getAll Tests")
  class GetAllTests {
    @Test
    @DisplayName("should only return active products")
    void shouldOnlyActiveProducts() {
      // Arrange
      ProductEntity inactive = ProductEntity.builder().id(1001L).isActived(false).build();
      when(productRepository.findAll()).thenReturn(List.of(product, inactive));
      when(s3Adapter.getUrl("prod.jpg")).thenReturn("http://s3.com/prod.jpg");

      // Act
      List<ProductResponseDto> result = productService.getAll();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getName()).isEqualTo("Product 1");
    }

    @Test
    @DisplayName("should return empty list when no products")
    void shouldReturnEmptyList() {
      // Arrange
      when(productRepository.findAll()).thenReturn(Collections.emptyList());

      // Act
      List<ProductResponseDto> result = productService.getAll();

      // Assert
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("createProduct Tests")
  class CreateProductTests {
    @Test
    @DisplayName("should create product successfully")
    void shouldCreateProductSuccessfully() {
      // Arrange
      when(imageRepository.findAllById(any())).thenReturn(List.of(image));
      when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
      when(categoryRepository.findByIdAndIsActive(10L, true)).thenReturn(Optional.of(category));
      when(productRepository.save(any(ProductEntity.class))).thenReturn(product);
      when(s3Adapter.getUrl("prod.jpg")).thenReturn("http://s3.com/prod.jpg");

      // Act
      ProductResponseDto result = productService.createProduct(productRequestDto);

      // Assert
      assertThat(result.getName()).isEqualTo("Product 1");
      verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    @DisplayName("should throw error when dates are invalid")
    void shouldThrowOnInvalidDates() {
      // Arrange
      productRequestDto.setStartDate(LocalDate.now().minusDays(1)); // yesterday

      // Act & Assert
      assertThatThrownBy(() -> productService.createProduct(productRequestDto))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Start date must be today or later");
    }

    @Test
    @DisplayName("should throw error when end date before start date")
    void shouldThrowOnEndDateBeforeStartDate() {
      // Arrange
      productRequestDto.setEndDate(productRequestDto.getStartDate().minusDays(1));

      // Act & Assert
      assertThatThrownBy(() -> productService.createProduct(productRequestDto))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("End date must be after or equal start date");
    }
  }

  @Nested
  @DisplayName("getById Tests")
  class GetByIdTests {
    @Test
    @DisplayName("should return detail successfully")
    void shouldReturnDetail() {
      // Arrange
      when(productRepository.findById(1000L)).thenReturn(Optional.of(product));
      when(s3Adapter.getUrl(any())).thenReturn("http://s3.com/prod.jpg");

      // Act
      ProductDetailResponseDto result = productService.getById(1000L);

      // Assert
      assertThat(result.getName()).isEqualTo("Product 1");
      assertThat(result.getImages()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("deleteById Tests")
  class DeleteByIdTests {
    @Test
    @DisplayName("should soft delete product")
    void shouldSoftDelete() {
      // Arrange
      when(productRepository.findById(1000L)).thenReturn(Optional.of(product));

      // Act
      productService.deleteById(1000L);

      // Assert
      assertThat(product.getIsActived()).isFalse();
      verify(productRepository).save(product);
    }

    @Test
    @DisplayName("should throw error if sold out")
    void shouldThrowIfSoldOut() {
      // Arrange
      product.setStatus(ProductStatus.SOLD_OUT);
      when(productRepository.findById(1000L)).thenReturn(Optional.of(product));

      // Act & Assert
      assertThatThrownBy(() -> productService.deleteById(1000L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Cannot delete a sold out product");
    }
  }

  @Nested
  @DisplayName("updateById Tests")
  class UpdateByIdTests {
    @Test
    @DisplayName("should update product successfully")
    void shouldUpdateSuccessfully() {
      // Arrange
      when(imageRepository.findAllById(any())).thenReturn(List.of(image, image, image, image));
      when(productRepository.findById(1000L)).thenReturn(Optional.of(product));
      when(categoryRepository.findByIdAndIsActive(10L, true)).thenReturn(Optional.of(category));
      when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
      when(productRepository.save(any(ProductEntity.class))).thenReturn(product);

      // Act
      ProductResponseDto result = productService.updateById(1000L, productRequestDto);

      // Assert
      verify(productRepository).save(product);
      verify(cartItemRepository).updatePriceByProductId(eq(1000L), any());
    }
  }

  @Nested
  @DisplayName("liquidationProduct Tests")
  class LiquidationProductTests {
    @Test
    @DisplayName("should set status to liquidation")
    void shouldLiquidation() {
      // Arrange
      when(productRepository.findById(1000L)).thenReturn(Optional.of(product));
      when(productRepository.save(any())).thenReturn(product);

      // Act
      productService.liquidationProduct(1000L);

      // Assert
      assertThat(product.getStatus()).isEqualTo(ProductStatus.LIQUIDATION);
    }
  }

  @Nested
  @DisplayName("search Tests")
  class SearchTests {
    @Test
    @DisplayName("should return paginated result")
    void shouldSearch() {
      // Arrange
      ProductSearchDto searchDto = new ProductSearchDto();
      searchDto.setPage(0);
      searchDto.setSize(10);
      searchDto.setName("Prod");

      Pageable pageable = PageRequest.of(0, 10);
      Page<ProductEntity> page = new PageImpl<>(List.of(product), pageable, 1);
      when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
          .thenReturn(page);

      // Act
      Pagination<ProductResponseDto> result = productService.search(searchDto);

      // Assert
      assertThat(result.getData()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("getProductsByUserId Tests")
  class GetProductsByUserIdTests {
    @Test
    @DisplayName("should return user products")
    void shouldReturnUserProducts() {
      // Arrange
      when(productRepository.findByCustomerIdAndIsActivedTrue(1L)).thenReturn(List.of(product));
      when(s3Adapter.getUrl(any())).thenReturn("http://s3.com/prod.jpg");

      // Act
      List<ProductResponseDto> result = productService.getProductsByUserId(1L);

      // Assert
      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("Status Update Tests")
  class StatusUpdateTests {
    @Test
    @DisplayName("updateStatusIsProgress should update NEW to IN_PROGRESS")
    void updateStatusIsProgress() {
      // Arrange
      when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(product));

      // Act
      productService.updateStatusIsProgress();

      // Assert
      assertThat(product.getStatus()).isEqualTo(ProductStatus.IN_PROGRESS);
      verify(productRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("updateStatusIsExpired should update IN_PROGRESS to EXPIRED")
    void updateStatusIsExpired() {
      // Arrange
      product.setStatus(ProductStatus.IN_PROGRESS);
      when(productRepository.findAll(any(Specification.class))).thenReturn(List.of(product));

      // Act
      productService.updateStatusIsExpired();

      // Assert
      assertThat(product.getStatus()).isEqualTo(ProductStatus.EXPIRED);
      verify(productRepository).saveAll(anyList());
    }
  }
}
