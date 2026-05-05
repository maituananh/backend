package com.spring.backend.domain.category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(Long id);
    Category save(Category category);
    List<Category> findAll();
}
