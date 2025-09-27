package com.spring.backend.service;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.entity.ImageEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.repository.ImageRepository;
import com.spring.backend.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ImageRepository imageRepository;
  private final S3Adapter s3Adapter;

  public List<ProductResponseDto> getAll() {
    List<ProductEntity> productEntities = productRepository.findAll();

    List<ProductResponseDto> dtos = new ArrayList<>();
    for (ProductEntity productEntity : productEntities) {
      List<ImageResponseDto> images =
          productEntity.getImages().stream()
              .map(
                  image ->
                      ImageResponseDto.builder()
                          .id(image.getId())
                          .url(s3Adapter.getUrl(image.getFileName()))
                          .build())
              .toList();

      ProductResponseDto dto =
          ProductResponseDto.builder()
              .name(productEntity.getName())
              .price(productEntity.getPrice())
              .id(productEntity.getId())
              .type(productEntity.getType())
              .startDay(productEntity.getStartDay())
              .endDate(productEntity.getEndDate())
              .type(productEntity.getType())
              .images(images)
              .build();

      dtos.add(dto);
    }

    return dtos;
  }

  public ProductResponseDto createProduct(ProductRequestDto dto) {
    ProductEntity productEntity = toProductEntity(dto);

    ProductEntity productUpdated = productRepository.save(productEntity);
    return new ProductResponseDto(productUpdated);
  }

  public ProductResponseDto getById(Long id) {
    ProductEntity productEntity = productRepository.findById(id).get();
    return new ProductResponseDto(productEntity);
  }

  public Page<ProductResponseDto> search(String name, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<ProductEntity> pageProductEntity =
        productRepository.findByNameLikeIgnoreCase(name, pageable);

    List<ProductResponseDto> productResponseDtos = new ArrayList<>();
    for (ProductEntity productEntity : pageProductEntity.getContent()) {
      productResponseDtos.add(new ProductResponseDto(productEntity));
    }

    return new PageImpl<>(productResponseDtos, pageable, pageProductEntity.getTotalElements());
  }

  public Page<ProductResponseDto> searchByType(String type, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("startDay").descending());
    Page<ProductEntity> pageProductEntity = productRepository.findByType(type, pageable);

    List<ProductResponseDto> productResponseDtos = new ArrayList<>();
    for (ProductEntity productEntity : pageProductEntity.getContent()) {
      productResponseDtos.add(new ProductResponseDto(productEntity));
    }

    return new PageImpl<>(productResponseDtos, pageable, pageProductEntity.getTotalElements());
  }

  public void deleteById(Long id) {
    productRepository.deleteById(id);
  }

  public ProductResponseDto updateById(Long id, ProductRequestDto dto) {
    ProductEntity productEntity = toProductEntity(dto);
    productEntity.setId(id);

    return new ProductResponseDto(productRepository.save(productEntity));
  }

  private ProductEntity toProductEntity(ProductRequestDto dto) {
    List<ImageEntity> imageEntities = imageRepository.findAllById(dto.getImageIds());

    ProductEntity product =
        ProductEntity.builder()
            .name(dto.getName())
            .price(dto.getPrice())
            .startDay(dto.getStartDay())
            .endDate(dto.getEndDate())
            .type(dto.getType())
            .build();

    product.setImages(imageEntities);

    return product;
  }
}
