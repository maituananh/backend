package com.spring.backend.infrastructure.mapper;

import com.spring.backend.domain.product.Product;
import com.spring.backend.domain.product.ProductQuantity;
import com.spring.backend.infrastructure.entity.ProductEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Entity → Domain: fully delegated to reconstitute() via @ObjectFactory
    // @BeanMapping(ignoreByDefault=true) prevents MapStruct from calling setters on domain aggregate
    @BeanMapping(ignoreByDefault = true)
    Product toDomain(ProductEntity entity);

    @ObjectFactory
    default Product createProduct(ProductEntity entity) {
        ProductQuantity quantity = new ProductQuantity(
            entity.getStockQty(),
            entity.getReservedQty()
        );
        return Product.reconstitute(
            entity.getId(),
            entity.getName(),
            entity.getPrice(),
            quantity,
            entity.getStatus(),
            entity.getIsActived(),
            entity.getCategory() != null ? entity.getCategory().getId() : null,
            entity.getCustomer() != null ? entity.getCustomer().getId() : null
        );
    }

    // Domain → Entity: standard MapStruct mapping using Lombok-generated setters
    // stockQty and reservedQty map from ProductQuantity fields
    // availableQty computed from ProductQuantity.availableQty() method
    // status: same enum type (domain.enums.ProductStatus) — MapStruct maps directly
    // category, customer, images: set by adapter (cannot set from domain ID alone without EntityManager)
    @Mapping(target = "stockQty", source = "quantity.stockQty")
    @Mapping(target = "reservedQty", source = "quantity.reservedQty")
    @Mapping(target = "availableQty",
             expression = "java(product.getQuantity().availableQty())")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "images", ignore = true)
    ProductEntity toEntity(Product product);
}
