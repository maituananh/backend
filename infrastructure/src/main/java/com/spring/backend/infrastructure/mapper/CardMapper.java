package com.spring.backend.infrastructure.mapper;

import com.spring.backend.domain.card.Card;
import com.spring.backend.infrastructure.entity.CardEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @BeanMapping(ignoreByDefault = true)
    Card toDomain(CardEntity entity);

    @ObjectFactory
    default Card createCard(CardEntity entity) {
        return Card.reconstitute(
            entity.getId(),
            entity.getCustomer() != null ? entity.getCustomer().getId() : null,
            entity.getNumberOfCard()
        );
    }

    // Domain → Entity: customer relation is set by the adapter (requires managed entity reference)
    @Mapping(target = "customer", ignore = true)
    CardEntity toEntity(Card card);
}
