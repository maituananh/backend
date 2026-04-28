package com.spring.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.spring.backend.adapter.s3.S3Adapter;
import com.spring.backend.service.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
public abstract class BaseIntegrationTest {

  @Autowired protected MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;
  @Autowired protected WireMockServer wireMockServer;

  @MockitoBean protected PaymentGatewayService paymentGatewayService;
  @MockitoBean protected S3Adapter s3Adapter;
}
