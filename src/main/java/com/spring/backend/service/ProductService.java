package com.spring.backend.service;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.dto.page.Pagination;
import com.spring.backend.dto.product.ProductDetailResponseDto;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.entity.CategoryEntity;
import com.spring.backend.entity.ImageEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.ProductStatus;
import com.spring.backend.repository.CategoryRepository;
import com.spring.backend.repository.ImageRepository;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.mapper.PageMapper;
import com.spring.backend.service.mapper.ProductMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {

  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final ImageRepository imageRepository;
  private final S3Adapter s3Adapter;

  public List<ProductResponseDto> getAll() {
    List<ProductEntity> entities = productRepository.findAll();

    return entities.stream()
        .map(
            p -> {
              if (CollectionUtils.isEmpty(p.getImages())) {
                return ProductMapper.toProductResponse(p, null);
              }
              String image = getImage(p.getImages().getFirst().getFileName());
              return ProductMapper.toProductResponse(p, image);
            })
        .toList();
  }

  @Transactional
  public ProductResponseDto createProduct(ProductRequestDto dto) {
    List<ImageEntity> imageEntities = imageRepository.findAllById(dto.getImageIds());
    UserEntity userEntity = userRepository.findById(dto.getCustomerId()).orElseThrow();

    CategoryEntity categoryEntity =
        categoryRepository
            .findByIdAndIsActive(dto.getCategoryId(), true)
            .orElseThrow(() -> new RuntimeException("Category is inactive or not found"));

    ProductEntity productEntity =
        ProductMapper.toProductEntity(dto, imageEntities, categoryEntity, userEntity);

    ProductEntity productUpdated = productRepository.save(productEntity);

    return ProductMapper.toProductResponse(
        productUpdated, getImage(imageEntities.getFirst().getFileName()));
  }

  public ProductDetailResponseDto getById(Long id) {
    ProductEntity productEntity = productRepository.findById(id).orElseThrow();

    List<ImageResponseDto> images =
        productEntity.getImages().stream()
            .map(
                i ->
                    ImageResponseDto.builder()
                        .url(s3Adapter.getUrl(i.getFileName()))
                        .id(i.getId())
                        .build())
            .toList();

    return ProductMapper.toProductDetailResponse(productEntity, images);
  }

  public Pagination<ProductResponseDto> search(
      String name,
      ProductStatus status,
      Double price,
      LocalDate startDay,
      LocalDate endDay,
      String code,
      List<Integer> categoryIds,
      Integer page,
      Integer size) {
    Specification<ProductEntity> spec =
        ProductRepository.search(name, status, price, startDay, endDay, code, categoryIds);

    Pageable pageable = PageMapper.getPageable(page, size);

    Page<ProductEntity> pageProductEntity = productRepository.findAll(spec, pageable);

    List<ProductResponseDto> productResponseDtos = new ArrayList<>();
    for (ProductEntity productEntity : pageProductEntity.getContent()) {
      String imageUrl = null;
      if (productEntity.getImages() != null && !productEntity.getImages().isEmpty()) {
        imageUrl = getImage(productEntity.getImages().getFirst().getFileName());
      }
      productResponseDtos.add(ProductMapper.toProductResponse(productEntity, imageUrl));
    }

    return PageMapper.toPagination(pageProductEntity, productResponseDtos);
  }

  public Page<ProductResponseDto> searchByType(String type, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("startDay").descending());
    Page<ProductEntity> pageProductEntity = productRepository.findByType(type, pageable);

    List<ProductResponseDto> productResponseDtos = new ArrayList<>();
    for (ProductEntity productEntity : pageProductEntity.getContent()) {
      productResponseDtos.add(ProductMapper.toProductResponse(productEntity, null));
    }

    return new PageImpl<>(productResponseDtos, pageable, pageProductEntity.getTotalElements());
  }

  public List<ProductResponseDto> getProductsByUserId(Long userId) {
    List<ProductEntity> products = productRepository.findByCustomerId(userId);

    List<ProductResponseDto> product = new ArrayList<>();

    for (ProductEntity p : products) {
      String image = null;
      if (!p.getImages().isEmpty()) {
        image = getImage(p.getImages().getFirst().getFileName());
      }
      product.add(ProductMapper.toProductResponse(p, image));
    }

    return product;
  }

  @Transactional
  public void deleteById(Long id) {
    productRepository.deleteById(id);
  }

  @Transactional
  public ProductResponseDto updateById(Long id, ProductRequestDto dto) {
    ProductEntity productEntity = productRepository.findById(id).orElseThrow();
    CategoryEntity categoryEntity =
        categoryRepository.findByIdAndIsActive(dto.getCategoryId(), true).orElseThrow();
    UserEntity userEntity = userRepository.findById(dto.getCustomerId()).orElseThrow();

    List<ImageEntity> imageEntities = imageRepository.findAllById(dto.getImageIds());

    if (imageEntities.size() != 4) {
      throw new IllegalArgumentException("Some images not found");
    }

    productEntity.setName(dto.getName());
    productEntity.setPrice(dto.getPrice());
    productEntity.setStartDate(dto.getStartDate());
    productEntity.setEndDate(dto.getEndDate());
    productEntity.setType(dto.getType());
    productEntity.setDescription(dto.getDescription());
    productEntity.setQuantity(dto.getQuantity());
    productEntity.setCategory(categoryEntity);
    productEntity.setCustomer(userEntity);
    productEntity.setImages(imageEntities);

    productEntity.getImages().clear();
    productEntity.setImages(imageEntities);

    if (dto.getCode() != null) {
      productEntity.setCode(dto.getCode());
    }

    ProductEntity saved = productRepository.save(productEntity);

    String imageUrl = null;
    if (!imageEntities.isEmpty()) {
      imageUrl = getImage(imageEntities.getFirst().getFileName());
    }

    return ProductMapper.toProductResponse(saved, imageUrl);
  }

  @Transactional
  public ProductResponseDto liquidationProduct(Long id) {
    ProductEntity productEntity =
        productRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

    productEntity.setStatus(ProductStatus.LIQUIDATION);

    ProductEntity updated = productRepository.save(productEntity);

    String imageUrl = null;
    if (!updated.getImages().isEmpty()) {
      imageUrl = getImage(updated.getImages().getFirst().getFileName());
    }

    return ProductMapper.toProductResponse(updated, imageUrl);
  }

  @Transactional
  public void updateStatusIsProgress() {
    LocalDate today = LocalDate.now();

    List<ProductEntity> productEntities =
        productRepository.findAll(
            ProductRepository.findByDateAndStatus(
                "startDate", today.minusDays(2), ProductStatus.NEW));

    productEntities.forEach(productEntity -> productEntity.setStatus(ProductStatus.IN_PROGRESS));

    productRepository.saveAll(productEntities);
  }

  @Transactional
  public void updateStatusIsExpired() {
    LocalDate today = LocalDate.now();

    List<ProductEntity> productEntities =
        productRepository.findAll(
            ProductRepository.findByDateAndStatus("endDate", today, ProductStatus.IN_PROGRESS));

    productEntities.forEach(productEntity -> productEntity.setStatus(ProductStatus.EXPIRED));

    productRepository.saveAll(productEntities);
  }

  private String getImage(String fileName) {
    return s3Adapter.getUrl(fileName);
  }
}
