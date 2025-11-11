package com.spring.backend.service;

import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.repository.ProductRepository;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  @Override
  public ProductResponseDto createProduct(ProductRequestDto dto) {
    ProductEntity entity =
        ProductEntity.builder()
            .name(dto.getName())
            .code(dto.getCode())
            .type(dto.getType())
            .price(dto.getPrice())
            .dailyProfit(dto.getDailyProfit())
            .quantity(dto.getQuantity())
            .startedAt(dto.getStartedAt())
            .endAt(dto.getEndAt())
            .categoryId(dto.getCategoryId())
            .description(dto.getDescription())
            .customerId(dto.getCustomerId())
            .createdAt(Instant.now())
            .createdBy(1L)
            .imageIds(dto.getImageIds())
            .build();

    productRepository.save(entity);
    return toResponseDto(entity);
  }

  @Override
  public List<ProductResponseDto> getAll() {
    return productRepository.findAll().stream()
        .map(this::toResponseDto)
        .collect(Collectors.toList());
  }

  @Override
  public ProductResponseDto getById(Long id) {
    ProductEntity entity =
        productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    return toResponseDto(entity);
  }

  @Override
  public ProductResponseDto updateById(Long id, ProductRequestDto dto) {
    ProductEntity entity =
        productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

    entity.setName(dto.getName());
    entity.setCode(dto.getCode());
    entity.setType(dto.getType());
    entity.setPrice(dto.getPrice());
    entity.setDailyProfit(dto.getDailyProfit());
    entity.setQuantity(dto.getQuantity());
    entity.setStartedAt(dto.getStartedAt());
    entity.setEndAt(dto.getEndAt());
    entity.setCategoryId(dto.getCategoryId());
    entity.setDescription(dto.getDescription());
    entity.setCustomerId(dto.getCustomerId());
    entity.setUpdatedAt(Instant.now());
    entity.setUpdatedBy(1L);
    entity.setImageIds(dto.getImageIds());

    productRepository.save(entity);
    return toResponseDto(entity);
  }

  @Override
  public void deleteById(Long id) {
    ProductEntity entity =
        productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    productRepository.delete(entity);
  }

  private ProductResponseDto toResponseDto(ProductEntity entity) {
    return ProductResponseDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .code(entity.getCode())
        .type(entity.getType())
        .price(entity.getPrice())
        .dailyProfit(entity.getDailyProfit())
        .quantity(entity.getQuantity())
        .startDay(entity.getStartedAt())
        .endDate(entity.getEndAt())
        .categoryId(entity.getCategoryId())
        .description(entity.getDescription())
        .customerId(entity.getCustomerId())
        .createdAt(entity.getCreatedAt())
        .createdBy(entity.getCreatedBy())
        .updatedAt(entity.getUpdatedAt())
        .updatedBy(entity.getUpdatedBy())
        .imageIds(entity.getImageIds())
        .build();
  }
}
