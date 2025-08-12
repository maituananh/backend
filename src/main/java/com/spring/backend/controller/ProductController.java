package com.spring.backend.controller;

import com.spring.backend.dto.ProductDto;
import com.spring.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ProductDto createProduct(@RequestBody ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @GetMapping
    public List<ProductDto> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable("id") Long id) {
        return productService.getById(id);
    }

    @GetMapping("/search")
    public List<ProductDto> searchProduct(@RequestParam("name") String name) {
        return productService.search(name);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        productService.deleteById(id);
    }

    @PutMapping("/{id}")
    public ProductDto updateById(@PathVariable("id") Long id,
                           @RequestBody ProductDto productDto) {
        return productService.updateById(id, productDto);
    }
}
