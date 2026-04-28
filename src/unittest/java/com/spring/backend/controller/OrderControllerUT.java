package com.spring.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spring.backend.controller.order.OrderController;
import com.spring.backend.dto.checkout.CheckoutRequest;
import com.spring.backend.dto.checkout.CheckoutResponse;
import com.spring.backend.dto.order.OrderDetailResponse;
import com.spring.backend.dto.order.OrderStatusResponse;
import com.spring.backend.dto.page.Pagination;
import com.spring.backend.enums.PaymentMethod;
import com.spring.backend.exception.DuplicateWebhookEventException;
import com.spring.backend.service.OrderService;
import com.spring.backend.service.PaymentGatewayService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderControllerUT {

  private MockMvc mockMvc;

  @Mock private OrderService orderService;
  @Mock private PaymentGatewayService paymentGatewayService;

  @InjectMocks private OrderController orderController;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(orderController)
            .setMessageConverters(
                new StringHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
  }

  @Test
  @DisplayName("checkout should return response")
  void checkout_Works() throws Exception {
    CheckoutRequest request = new CheckoutRequest();
    request.setCartItemIds(List.of(1L));
    request.setPaymentMethod(PaymentMethod.STRIPE);
    request.setShippingName("Name");
    request.setShippingPhone("123");
    request.setShippingAddress("Address");

    CheckoutResponse response = CheckoutResponse.builder().orderId(100L).build();

    when(orderService.checkout(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(100));
  }

  @Test
  @DisplayName("getStatus should return status")
  void getStatus_Works() throws Exception {
    OrderStatusResponse response = OrderStatusResponse.builder().orderId(100L).build();

    when(orderService.getOrderStatus(100L)).thenReturn(response);

    mockMvc
        .perform(get("/api/orders/100/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(100));
  }

  @Test
  @DisplayName("webhook should handle success")
  void webhook_Success() throws Exception {
    Event event = mock(Event.class);
    when(paymentGatewayService.verifyAndConstructEvent(anyString(), anyString())).thenReturn(event);

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "sig")
                .contentType(MediaType.TEXT_PLAIN)
                .content("payload"))
        .andExpect(status().isOk());

    verify(orderService).handleWebhook(eq(event), eq("payload"));
  }

  @Test
  @DisplayName("webhook should handle signature error")
  void webhook_SignatureError() throws Exception {
    when(paymentGatewayService.verifyAndConstructEvent(anyString(), anyString()))
        .thenThrow(new SignatureVerificationException("error", "sig"));

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "sig")
                .contentType(MediaType.TEXT_PLAIN)
                .content("payload"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("webhook should handle duplicate error")
  void webhook_DuplicateError() throws Exception {
    Event event = mock(Event.class);
    when(paymentGatewayService.verifyAndConstructEvent(anyString(), anyString())).thenReturn(event);
    doThrow(new DuplicateWebhookEventException("id"))
        .when(orderService)
        .handleWebhook(any(), any());

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "sig")
                .contentType(MediaType.TEXT_PLAIN)
                .content("payload"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("webhook should handle integrity error")
  void webhook_IntegrityError() throws Exception {
    Event event = mock(Event.class);
    when(paymentGatewayService.verifyAndConstructEvent(anyString(), anyString())).thenReturn(event);
    doThrow(new DataIntegrityViolationException("error"))
        .when(orderService)
        .handleWebhook(any(), any());

    mockMvc
        .perform(
            post("/api/payment/webhook")
                .header("Stripe-Signature", "sig")
                .contentType(MediaType.TEXT_PLAIN)
                .content("payload"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("getOrders should return list")
  void getOrders_Works() throws Exception {
    when(orderService.getOrders())
        .thenReturn(List.of(OrderDetailResponse.builder().orderId(1L).build()));

    mockMvc
        .perform(get("/api/orders/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].orderId").value(1));
  }

  @Test
  @DisplayName("getMyOrdersPaginated should return pagination")
  void getMyOrdersPaginated_Works() throws Exception {
    Pagination<OrderDetailResponse> response =
        Pagination.<OrderDetailResponse>builder().totalPages(1).build();
    when(orderService.getMyOrdersPaginated(anyInt(), anyInt(), any())).thenReturn(response);

    mockMvc
        .perform(get("/api/orders").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  @DisplayName("getAllOrdersPaginatedForAdmin should return pagination")
  void getAllOrdersPaginatedForAdmin_Works() throws Exception {
    Pagination<OrderDetailResponse> response =
        Pagination.<OrderDetailResponse>builder().totalPages(1).build();
    when(orderService.getAllOrdersPaginatedForAdmin(anyInt(), anyInt(), any()))
        .thenReturn(response);

    mockMvc
        .perform(get("/api/admin/orders").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  @DisplayName("getOrderDetail should return detail")
  void getOrderDetail_Works() throws Exception {
    OrderDetailResponse response = OrderDetailResponse.builder().orderId(100L).build();
    when(orderService.getOrderDetail(100L)).thenReturn(response);

    mockMvc
        .perform(get("/api/orders/100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(100));
  }

  @Test
  @DisplayName("cancelOrder should return ok")
  void cancelOrder_Works() throws Exception {
    doNothing().when(orderService).cancelOrder(100L);

    mockMvc.perform(post("/api/orders/100/cancel")).andExpect(status().isOk());
  }
}
