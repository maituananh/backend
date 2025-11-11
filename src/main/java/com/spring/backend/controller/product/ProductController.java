package com.spring.backend.controller.product;

import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.service.ProductService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  @Autowired
  private ProductService productService;

  @PostMapping
  public ProductResponseDto createProduct(@RequestBody ProductRequestDto dto) {
    return productService.createProduct(dto);
  }

  @GetMapping
  public List<ProductResponseDto> getAll() {
    return productService.getAll();
  }

  @GetMapping("/{id}")
  public ProductResponseDto getById(@PathVariable("id") Long id) {
    return productService.getById(id);
  }

  @GetMapping("/search")
  public Page<ProductResponseDto> searchProduct(
      @RequestParam("page") int page,
      @RequestParam("size") int size,
      @RequestParam("name") String name) {
    return productService.search(name, page, size);
  }

  @GetMapping("/search-by-type")
  public Page<ProductResponseDto> searchByType(
      @RequestParam String type, @RequestParam int page, @RequestParam int size) {
    return productService.searchByType(type, page, size);
  }

  @DeleteMapping("/{id}")
  public void deleteById(@PathVariable("id") Long id) {
    productService.deleteById(id);
  }

  @PutMapping("/{id}")
  public ProductResponseDto updateById(
      @PathVariable("id") Long id, @RequestBody ProductRequestDto dto) {
    return productService.updateById(id, dto);
  }
}
