package com.spring.backend.service;

import com.spring.backend.dto.cart.AddToCartRequestDto;
import com.spring.backend.dto.cart.CartResponseDto;
import com.spring.backend.entity.CartEntity;
import com.spring.backend.entity.CartItemEntity;
import com.spring.backend.entity.ProductEntity;
import com.spring.backend.entity.UserEntity;
import com.spring.backend.enums.CartItemStatus;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.CartItemRepository;
import com.spring.backend.repository.CartRepository;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.mapper.CartMapper;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cardRepository;
  private final CartItemRepository cardItemRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final UserHelper userHelper;

  @Transactional
  public CartResponseDto addToCard(AddToCartRequestDto dto) {

    if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
      throw new RuntimeException("Quantity must be greater than 0");
    }
    Long customerId = userHelper.getCurrentUserId();
    UserEntity customer =
        userRepository
            .findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    CartEntity card =
        cardRepository
            .findByCustomerId(customerId)
            .orElseGet(() -> cardRepository.save(CartEntity.builder().customer(customer).build()));

    ProductEntity product =
        productRepository
            .findById(dto.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

    Optional<CartItemEntity> existingItem =
        cardItemRepository.findByCard_IdAndProduct_Id(card.getId(), product.getId());

    if (existingItem.isPresent()) {
      CartItemEntity item = existingItem.get();
      item.setQuantity(item.getQuantity() + dto.getQuantity());
    } else {
      CartItemEntity newItem =
          CartItemEntity.builder()
              .card(card)
              .product(product)
              .price(product.getPrice())
              .quantity(dto.getQuantity())
              .status(CartItemStatus.PENDING)
              .build();

      card.getItems().add(newItem);
    }
    return CartMapper.toCardDto(card);
  }

  @Transactional
  public CartResponseDto getMyCard(Long customerId) {

    CartEntity card =
        cardRepository
            .findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Card not found"));

    return CartMapper.toCardDto(card);
  }
}
