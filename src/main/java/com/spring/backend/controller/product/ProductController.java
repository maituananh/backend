package com.spring.backend.controller.product;

import com.spring.backend.dto.page.Pagination;
import com.spring.backend.dto.product.ProductDetailResponseDto;
import com.spring.backend.dto.product.ProductRequestDto;
import com.spring.backend.dto.product.ProductResponseDto;
import com.spring.backend.enums.ProductStatus;
import com.spring.backend.service.ProductService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  @Autowired private ProductService productService;

  @PostMapping
  public ProductResponseDto createProduct(@RequestBody @Valid ProductRequestDto dto) {
    return productService.createProduct(dto);
  }

  @GetMapping
  public List<ProductResponseDto> getAll() {
    return productService.getAll();
  }

  @GetMapping("/{id}")
  public ProductDetailResponseDto getById(@PathVariable("id") Long id) {
    return productService.getById(id);
  }

  @GetMapping("/search")
  public Pagination<ProductResponseDto> searchProduct(
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size,
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "status", required = false) ProductStatus status,
      @RequestParam(value = "price", required = false) Double price,
      @RequestParam(value = "startDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(value = "endDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "categoryIds", required = false) List<Integer> categoryIds) {
    return productService.search(
        name, status, price, startDate, endDate, code, categoryIds, page, size);
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
      @PathVariable("id") Long id, @RequestBody @Valid ProductRequestDto dto) {
    return productService.updateById(id, dto);
  }

  @PatchMapping("/{id}/liquidation")
  public ProductResponseDto liquidationProduct(@PathVariable Long id) {
    return productService.liquidationProduct(id);
  }
}
