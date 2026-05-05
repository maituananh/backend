package com.spring.backend.infrastructure.mapper;

import com.spring.backend.domain.category.Category;
import com.spring.backend.infrastructure.entity.CategoryEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @BeanMapping(ignoreByDefault = true)
    Category toDomain(CategoryEntity entity);

    @ObjectFactory
    default Category createCategory(CategoryEntity entity) {
        return Category.reconstitute(
            entity.getId(),
            entity.getName(),
            entity.getNote(),
            entity.getIsActive()
        );
    }

    // Domain → Entity: MapStruct auto-maps matching fields
    // products lazy collection is ignored — managed by JPA
    @Mapping(target = "products", ignore = true)
    CategoryEntity toEntity(Category category);
}
