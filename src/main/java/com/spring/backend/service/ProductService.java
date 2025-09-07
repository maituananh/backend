package com.spring.backend.service;

import com.spring.backend.dto.ProductDto;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

  @Autowired private ProductRepository productRepository;

  public List<ProductDto> getAll() {
    List<ProductEntity> productEntities = productRepository.findAll();

    List<ProductDto> productDtos = new ArrayList<>();
    for (ProductEntity productEntity : productEntities) {
      productDtos.add(new ProductDto(productEntity));
    }

    return productDtos;
  }

  public ProductDto createProduct(ProductDto productDto) {
    ProductEntity productEntity = new ProductEntity();
    productEntity.setName(productDto.getName());
    productEntity.setPrice(productDto.getPrice());
    productEntity.setStartDay(productDto.getStartDay());
    productEntity.setEndDate(productDto.getEndDate());
    productEntity.setType(productDto.getType());

    ProductEntity entitySaved = productRepository.save(productEntity);

    return new ProductDto(entitySaved);
  }

  public ProductDto getById(Long id) {
    ProductEntity productEntity = productRepository.findById(id).get();
    return new ProductDto(productEntity);
  }

  public Page<ProductDto> search(String name, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<ProductEntity> pageProductEntity =
        productRepository.findByNameLikeIgnoreCase(name, pageable);

    List<ProductDto> productDtos = new ArrayList<>();
    for (ProductEntity productEntity : pageProductEntity.getContent()) {
      productDtos.add(new ProductDto(productEntity));
    }

    return new PageImpl<>(productDtos, pageable, pageProductEntity.getTotalElements());
  }

  public void deleteById(Long id) {
    productRepository.deleteById(id);
  }

  public ProductDto updateById(Long id, ProductDto productDto) {
    ProductEntity productEntity = new ProductEntity();
    productEntity.setId(id);
    productEntity.setName(productDto.getName());
    productEntity.setPrice(productDto.getPrice());
    productEntity.setStartDay(productDto.getStartDay());
    productEntity.setEndDate(productDto.getEndDate());
    productEntity.setType(productDto.getType());

    ProductEntity productUpdated = productRepository.save(productEntity);
    return new ProductDto(productUpdated);
  }
}
