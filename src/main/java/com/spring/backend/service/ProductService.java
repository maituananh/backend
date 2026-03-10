package com.spring.backend.service;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.image.ImageResponseDto;
import com.spring.backend.dto.page.Pagination;
import com.spring.backend.dto.product.ProductDetailResponseDto;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.dto.product.ProductSearchDto;
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
import java.util.HashSet;
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

  private void validateProductDate(LocalDate startDate, LocalDate endDate) {
    LocalDate today = LocalDate.now();

    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start date and end date must not be null");
    }

    if (startDate.isBefore(today)) {
      throw new IllegalArgumentException("Start date must be today or later");
    }

    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date must be after or equal start date");
    }
  }

  public List<ProductResponseDto> getAll() {
    List<ProductEntity> entities =
        productRepository.findAll().stream().filter(ProductEntity::getIsActived).toList();

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

    validateProductDate(dto.getStartDate(), dto.getEndDate());

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

  public Pagination<ProductResponseDto> getRelatedProducts(Long id, Integer page, Integer size) {
    ProductEntity product = productRepository.findById(id).orElseThrow();
    Pageable pageable =
        PageMapper.getPageable(page, size, Sort.by(Sort.Direction.DESC, "startDate"));

    Page<ProductEntity> pageRelatedProducts =
        productRepository.findByCategoryIdAndStatusAndIdNotAndIsActivedTrue(
            product.getCategory().getId(), ProductStatus.LIQUIDATION, id, pageable);

    List<ProductResponseDto> productResponseDtos =
        pageRelatedProducts.getContent().stream()
            .map(
                p -> {
                  String image = null;
                  if (!CollectionUtils.isEmpty(p.getImages())) {
                    image = getImage(p.getImages().getFirst().getFileName());
                  }
                  return ProductMapper.toProductResponse(p, image);
                })
            .toList();

    return PageMapper.toPagination(pageRelatedProducts, productResponseDtos);
  }

  public Pagination<ProductResponseDto> search(ProductSearchDto searchDto) {
    Specification<ProductEntity> spec =
        ProductRepository.search(
            searchDto.getName(),
            searchDto.getStatus(),
            searchDto.getPrice(),
            searchDto.getStartDate(),
            searchDto.getEndDate(),
            searchDto.getCode(),
            searchDto.getCategoryIds());

    Pageable pageable = PageMapper.getPageable(searchDto.getPage(), searchDto.getSize());

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
    Page<ProductEntity> pageProductEntity =
        productRepository.findByTypeAndIsActivedTrue(type, pageable);

    List<ProductResponseDto> productResponseDtos = new ArrayList<>();
    for (ProductEntity productEntity : pageProductEntity.getContent()) {
      productResponseDtos.add(ProductMapper.toProductResponse(productEntity, null));
    }

    return new PageImpl<>(productResponseDtos, pageable, pageProductEntity.getTotalElements());
  }

  public List<ProductResponseDto> getProductsByUserId(Long userId) {
    List<ProductEntity> products = productRepository.findByCustomerIdAndIsActivedTrue(userId);

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
    ProductEntity product = productRepository.findById(id).orElseThrow();
    product.setIsActived(false);
    productRepository.save(product);
  }

  @Transactional
  public ProductResponseDto updateById(Long id, ProductRequestDto dto) {

    validateProductDate(dto.getStartDate(), dto.getEndDate());

    ProductEntity productEntity = productRepository.findById(id).orElseThrow();
    CategoryEntity categoryEntity =
        categoryRepository.findByIdAndIsActive(dto.getCategoryId(), true).orElseThrow();
    UserEntity userEntity = userRepository.findById(dto.getCustomerId()).orElseThrow();

    List<ImageEntity> currentImages = productEntity.getImages();

    if (dto.getImageIds() != null) {
      List<ImageEntity> newImages = imageRepository.findAllById(dto.getImageIds());

      if (newImages.size() != 4) {
        throw new IllegalArgumentException("Some images not found");
      }

      if (!new HashSet<>(currentImages).containsAll(newImages)
          || currentImages.size() != newImages.size()) {
        currentImages.clear();
        currentImages.addAll(newImages);
      }
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

    if (dto.getCode() != null) {
      productEntity.setCode(dto.getCode());
    }

    ProductEntity saved = productRepository.save(productEntity);
    return ProductMapper.toProductResponse(saved, null);
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
