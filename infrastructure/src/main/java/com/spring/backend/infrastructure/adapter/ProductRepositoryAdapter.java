package com.spring.backend.infrastructure.adapter;

import com.spring.backend.domain.product.Product;
import com.spring.backend.domain.product.ProductRepository;
import com.spring.backend.infrastructure.entity.ProductEntity;
import com.spring.backend.infrastructure.mapper.ProductMapper;
import com.spring.backend.infrastructure.repository.ProductJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Product> findByIdWithLock(Long id) {
        return jpaRepository.findByIdWithLock(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Product save(Product product) {
        // Load existing entity first to preserve @ManyToOne relations (category, customer, images)
        // that cannot be reconstructed from domain Product which only has categoryId/customerId as Long.
        // If new entity (id is null), create via mapper; otherwise load existing and update scalars.
        ProductEntity entity;
        if (product.getId() != null) {
            entity = jpaRepository.findById(product.getId())
                .orElseGet(() -> mapper.toEntity(product));
            // Update scalar fields from domain aggregate
            entity.setName(product.getName());
            entity.setPrice(product.getPrice());
            entity.setIsActived(product.getIsActived());
            entity.setCode(product.getCode());
            entity.setDescription(product.getDescription());
            entity.setStartDate(product.getStartDate());
            entity.setEndDate(product.getEndDate());
            entity.setDailyProfit(product.getDailyProfit());
            entity.setStockQty(product.getQuantity().getStockQty());
            entity.setReservedQty(product.getQuantity().getReservedQty());
            entity.setAvailableQty(product.getQuantity().availableQty());
            entity.setStatus(product.getStatus());
        } else {
            entity = mapper.toEntity(product);
        }
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
