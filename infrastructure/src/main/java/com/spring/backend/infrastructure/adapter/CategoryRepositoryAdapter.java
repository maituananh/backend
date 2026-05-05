package com.spring.backend.infrastructure.adapter;

import com.spring.backend.domain.category.Category;
import com.spring.backend.domain.category.CategoryRepository;
import com.spring.backend.infrastructure.mapper.CategoryMapper;
import com.spring.backend.infrastructure.repository.CategoryJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;
    private final CategoryMapper mapper;

    @Override
    public Optional<Category> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Category save(Category category) {
        var entity = mapper.toEntity(category);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
