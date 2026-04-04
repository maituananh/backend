package com.spring.backend.controller.product;

import com.spring.backend.dto.page.Pagination;
import com.spring.backend.dto.product.ProductDetailResponseDto;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.dto.product.ProductSearchDto;
import com.spring.backend.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @PostMapping
  public ProductResponseDto createProduct(@RequestBody @Valid ProductRequestDto dto) {
    return productService.createProduct(dto);
  }

  @GetMapping
  public List<ProductResponseDto> getAll() {
    return productService.getAll();
  }

  @GetMapping("/{id}")
  public ProductDetailResponseDto getById(@PathVariable Long id) {
    return productService.getById(id);
  }

  @GetMapping("/{id}/related")
  public Pagination<ProductResponseDto> getRelatedProducts(
      @PathVariable Long id,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return productService.getRelatedProducts(id, page, size);
  }

  @GetMapping("/search")
  public Pagination<ProductResponseDto> searchProduct(ProductSearchDto searchDto) {
    return productService.search(searchDto);
  }

  @DeleteMapping("/{id}")
  public void deleteById(@PathVariable Long id) {
    productService.deleteById(id);
  }

  @PutMapping("/{id}")
  public ProductResponseDto updateById(
      @PathVariable Long id, @RequestBody @Valid ProductRequestDto dto) {
    return productService.updateById(id, dto);
  }

  @PatchMapping("/{id}/liquidation")
  public ProductResponseDto liquidationProduct(@PathVariable Long id) {
    return productService.liquidationProduct(id);
  }
}
