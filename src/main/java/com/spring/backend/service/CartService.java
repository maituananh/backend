package com.spring.backend.service;

import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.dto.cart.AddToCartRequestDto;
import com.spring.backend.dto.cart.CartResponseDto;
import com.spring.backend.entity.*;
import com.spring.backend.enums.CartItemStatus;
import com.spring.backend.helper.UserHelper;
import com.spring.backend.repository.CartItemRepository;
import com.spring.backend.repository.CartRepository;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import com.spring.backend.service.mapper.CartMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final UserHelper userHelper;
  private final S3Adapter s3Adapter;

  private String getImage(CartItemEntity item) {
    if (item.getProduct() != null
        && item.getProduct().getImages() != null
        && !item.getProduct().getImages().isEmpty()) {
      return s3Adapter.getUrl(item.getProduct().getImages().getFirst().getFileName());
    }
    return null;
  }

  @Transactional
  public CartResponseDto addToCart(AddToCartRequestDto dto) {

    if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
      throw new RuntimeException("Quantity must be greater than 0");
    }
    Long customerId = userHelper.getCurrentUserId();
    UserEntity customer =
        userRepository
            .findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    CartEntity cart =
        cartRepository
            .findByCustomerId(customerId)
            .orElseGet(() -> cartRepository.save(CartEntity.builder().customer(customer).build()));

    ProductEntity product =
        productRepository
            .findById(dto.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

    Optional<CartItemEntity> existingItem =
        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

    if (existingItem.isPresent()) {
      CartItemEntity item = existingItem.get();
      item.setQuantity(item.getQuantity() + dto.getQuantity());
    } else {
      CartItemEntity newItem =
          CartItemEntity.builder()
              .cart(cart)
              .product(product)
              .price(BigDecimal.valueOf(product.getPrice()))
              .quantity(dto.getQuantity())
              .status(CartItemStatus.PENDING)
              .build();

      cart.getItems().add(newItem);
    }
    return CartMapper.toCartDto(cart, this::getImage);
  }

  public CartResponseDto getMyCart() {
    Long customerId = userHelper.getCurrentUserId();

    CartEntity cart = cartRepository.findByCustomerId(customerId).orElse(null);

    return cart == null ? null : CartMapper.toCartDto(cart, this::getImage);
  }

  @Transactional
  public void deleteItemOnCart(List<Long> productIds) {
    Long customerId = userHelper.getCurrentUserId();

    CartEntity cart =
        cartRepository
            .findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));

    List<CartItemEntity> itemsToRemove =
        cart.getItems().stream().filter(i -> productIds.contains(i.getProduct().getId())).toList();

    if (itemsToRemove.size() != productIds.size()) {
      throw new RuntimeException("Some items not found in cart");
    }

    itemsToRemove.forEach(cart.getItems()::remove);
  }
}
