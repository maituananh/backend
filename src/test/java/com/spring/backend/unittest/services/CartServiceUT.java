package com.spring.backend.unittest.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import com.spring.backend.service.CartService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceUT {

  @Mock private CartRepository cartRepository;
  @Mock private CartItemRepository cartItemRepository;
  @Mock private ProductRepository productRepository;
  @Mock private UserRepository userRepository;
  @Mock private UserHelper userHelper;
  @Mock private S3Adapter s3Adapter;
  @InjectMocks private CartService cartService;

  private UserEntity customer;
  private ProductEntity product;
  private CartEntity cart;
  private AddToCartRequestDto addToCartRequestDto;

  @BeforeEach
  void setUp() {
    customer = UserEntity.builder().id(1L).username("customer").build();
    product =
        ProductEntity.builder()
            .id(10L)
            .name("Product 1")
            .price(100.0)
            .images(new ArrayList<>())
            .build();
    cart = CartEntity.builder().id(100L).customer(customer).items(new ArrayList<>()).build();

    addToCartRequestDto = new AddToCartRequestDto();
    addToCartRequestDto.setProductId(10L);
    addToCartRequestDto.setQuantity(2);
  }

  @Nested
  @DisplayName("addToCart Tests")
  class AddToCartTests {
    @Test
    @DisplayName("should throw error when quantity is null or zero")
    void shouldThrowErrorWhenQuantityInvalid() {
      // Act & Assert
      addToCartRequestDto.setQuantity(null);
      assertThatThrownBy(() -> cartService.addToCart(addToCartRequestDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Quantity must be greater than 0");

      addToCartRequestDto.setQuantity(0);
      assertThatThrownBy(() -> cartService.addToCart(addToCartRequestDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Quantity must be greater than 0");
    }

    @Test
    @DisplayName("should add item to a new cart")
    void shouldAddIntoNewCart() {
      // Arrange
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
      when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.empty());
      when(cartRepository.save(any(CartEntity.class))).thenReturn(cart);
      when(productRepository.findById(10L)).thenReturn(Optional.of(product));
      when(cartItemRepository.findByCartIdAndProductId(100L, 10L)).thenReturn(Optional.empty());

      // Act
      CartResponseDto result = cartService.addToCart(addToCartRequestDto);

      // Assert
      assertThat(result.getItems()).hasSize(1);
      assertThat(result.getItems().get(0).getProductName()).isEqualTo("Product 1");
      assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("should increment quantity for existing item")
    void shouldIncrementExistingItem() {
      // Arrange
      CartItemEntity existingItem =
          CartItemEntity.builder()
              .product(product)
              .quantity(1)
              .status(CartItemStatus.PENDING)
              .build();
      cart.getItems().add(existingItem);

      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
      when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
      when(productRepository.findById(10L)).thenReturn(Optional.of(product));
      when(cartItemRepository.findByCartIdAndProductId(100L, 10L))
          .thenReturn(Optional.of(existingItem));

      // Act
      CartResponseDto result = cartService.addToCart(addToCartRequestDto);

      // Assert
      assertThat(result.getItems()).hasSize(1);
      assertThat(result.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("should throw error when customer not found")
    void shouldThrowErrorWhenCustomerNotFound() {
      // Arrange
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> cartService.addToCart(addToCartRequestDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("should throw error when product not found")
    void shouldThrowErrorWhenProductNotFound() {
      // Arrange
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
      when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
      when(productRepository.findById(10L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> cartService.addToCart(addToCartRequestDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Product not found");
    }

    @Test
    @DisplayName("should handle product with images correctly using s3Adapter")
    void shouldHandleProductImages() {
      // Arrange
      ImageEntity image = ImageEntity.builder().fileName("prod1.jpg").build();
      product.getImages().add(image);

      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
      when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
      when(productRepository.findById(10L)).thenReturn(Optional.of(product));
      when(cartItemRepository.findByCartIdAndProductId(100L, 10L)).thenReturn(Optional.empty());
      when(s3Adapter.getUrl("prod1.jpg")).thenReturn("http://s3.com/prod1.jpg");

      // Act
      CartResponseDto result = cartService.addToCart(addToCartRequestDto);

      // Assert
      assertThat(result.getItems().get(0).getImage()).isEqualTo("http://s3.com/prod1.jpg");
    }
  }

  @Nested
  @DisplayName("getMyCart Tests")
  class GetMyCartTests {
    @Test
    @DisplayName("should return cart when exists")
    void shouldReturnCart() {
      // Arrange
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

      // Act
      CartResponseDto result = cartService.getMyCart();

      // Assert
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("should return null when cart not found")
    void shouldReturnNull() {
      // Arrange
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.empty());

      // Act
      CartResponseDto result = cartService.getMyCart();

      // Assert
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("deleteItemOnCart Tests")
  class DeleteItemOnCartTests {
    @Test
    @DisplayName("should remove items from cart")
    void shouldRemoveItems() {
      // Arrange
      CartItemEntity item =
          CartItemEntity.builder().product(product).status(CartItemStatus.PENDING).build();
      cart.getItems().add(item);

      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

      // Act
      cartService.deleteItemOnCart(List.of(10L));

      // Assert
      assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("should throw error if cart not found")
    void shouldThrowErrorIfCartNotFound() {
      // Arrange
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> cartService.deleteItemOnCart(List.of(10L)))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Cart not found");
    }

    @Test
    @DisplayName("should throw error if some items not found in cart")
    void shouldThrowErrorIfItemsMissing() {
      // Arrange
      when(userHelper.getCurrentUserId()).thenReturn(1L);
      when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

      // Act & Assert
      assertThatThrownBy(() -> cartService.deleteItemOnCart(List.of(10L)))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Some items not found in cart");
    }
  }
}
