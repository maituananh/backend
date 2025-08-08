package com.spring.backend.service;

import com.spring.backend.dto.ProductDto;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductDto> getAll() {
        return productRepository.findAll().stream().map(ProductDto::new).toList();
    }

    public ProductDto createProduct(ProductDto productDto) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(productDto.getName());
        productEntity.setDescription(productDto.getDescription());
        productEntity.setPrice(productDto.getPrice());

        ProductEntity entitySaved = productRepository.save(productEntity);

        return new ProductDto(entitySaved);
    }
}
