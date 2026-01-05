package com.spring.backend.service;

import com.spring.backend.dto.card.AddToCardRequestDto;
import com.spring.backend.dto.card.CardResponseDto;
import com.spring.backend.entity.CardEntity;
import com.spring.backend.entity.CardItemEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.CardItemStatus;
import com.spring.backend.repository.CardItemRepository;
import com.spring.backend.repository.CardRepository;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.mapper.CardMapper;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardService {

  private final CardRepository cardRepository;
  private final CardItemRepository cardItemRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  @Transactional
  public CardResponseDto addToCard(Long customerId, AddToCardRequestDto dto) {

    if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
      throw new RuntimeException("Quantity must be greater than 0");
    }

    UserEntity customer =
        userRepository
            .findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    CardEntity card =
        cardRepository
            .findByCustomerId(customerId)
            .orElseGet(() -> cardRepository.save(CardEntity.builder().customer(customer).build()));

    ProductEntity product =
        productRepository
            .findById(dto.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

    Optional<CardItemEntity> existingItem =
        cardItemRepository.findByCard_IdAndProduct_Id(card.getId(), product.getId());

    if (existingItem.isPresent()) {
      CardItemEntity item = existingItem.get();
      item.setQuantity(item.getQuantity() + dto.getQuantity());
    } else {
      CardItemEntity newItem =
          CardItemEntity.builder()
              .card(card)
              .product(product)
              .price(product.getPrice())
              .quantity(dto.getQuantity())
              .status(CardItemStatus.PENDING)
              .build();

      card.getItems().add(newItem);
    }
    return CardMapper.toCardDto(card);
  }

  @Transactional
  public CardResponseDto getMyCard(Long customerId) {

    CardEntity card =
        cardRepository
            .findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Card not found"));

    return CardMapper.toCardDto(card);
  }
}
