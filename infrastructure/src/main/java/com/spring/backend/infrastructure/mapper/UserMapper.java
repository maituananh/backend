package com.spring.backend.infrastructure.mapper;

import com.spring.backend.domain.user.User;
import com.spring.backend.infrastructure.entity.UserEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @BeanMapping(ignoreByDefault = true)
    User toDomain(UserEntity entity);

    @ObjectFactory
    default User createUser(UserEntity entity) {
        return User.reconstitute(
            entity.getId(),
            entity.getUsername(),
            entity.getEmail(),
            entity.getPassword(),
            entity.getRole(),
            entity.getIsActive()
        );
    }

    // Domain → Entity: MapStruct auto-maps matching fields via Lombok setters
    // Lazy collections (products, orders) are ignored — set by JPA relationships
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "orders", ignore = true)
    UserEntity toEntity(User user);
}
