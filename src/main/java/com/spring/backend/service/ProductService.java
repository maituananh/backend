package com.spring.backend.service;

import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductService {
  List<ProductResponseDto> getAll();

  ProductResponseDto createProduct(ProductRequestDto dto);

  ProductResponseDto getById(Long id);

  Page<ProductResponseDto> search(String name, int page, int size);

  Page<ProductResponseDto> searchByType(String type, int page, int size);

  void deleteById(Long id);

  ProductResponseDto updateById(Long id, ProductRequestDto dto);
}
