package com.spring.backend.config;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Example illustrating how to use WireMock to mock external API calls (OpenAI, etc.).
 *
 * <p>This is a DEMO file - you can delete or keep it for reference. In practice, create separate
 * tests for each service/controller that needs testing.
 */
@DisplayName("WireMock Demo - Mock External API Calls")
class WireMockDemoIT extends BaseIntegrationTest {

  @Autowired private WireMockServer wireMockServer;

  @BeforeEach
  void setUp() {
    // Reset all stubs before each test to avoid interference
    wireMockServer.resetAll();
  }

  @Test
  @DisplayName("Demo: WireMock stub for OpenAI chat completion")
  void demo_stubOpenAiChatCompletion() {
    // GIVEN - Stub OpenAI API endpoint
    wireMockServer.stubFor(
        post(urlEqualTo("/v1/chat/completions"))
            .withHeader("Authorization", matching("Bearer .*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        {
                          "id": "chatcmpl-test123",
                          "object": "chat.completion",
                          "model": "gpt-4o-mini",
                          "choices": [{
                            "index": 0,
                            "message": {
                              "role": "assistant",
                              "content": "Xin chào! Tôi là trợ lý AI."
                            },
                            "finish_reason": "stop"
                          }],
                          "usage": {
                            "prompt_tokens": 10,
                            "completion_tokens": 20,
                            "total_tokens": 30
                          }
                        }
                        """)));

    // Verify stub was registered
    wireMockServer.verify(0, postRequestedFor(urlEqualTo("/v1/chat/completions")));

    // ℹ️ To test for real, call your project's endpoint via mockMvc,
    // and Spring AI will forward the request to wireMockServer instead of real OpenAI.
    // Example:
    // mockMvc.perform(post("/api/chat/send")
    //         .header("Authorization", "Bearer <jwt>")
    //         .contentType(MediaType.APPLICATION_JSON)
    //         .content("""{ "message": "Hello" }"""))
    //     .andDo(print())
    //     .andExpect(status().isOk())
    //     .andExpect(jsonPath("$.reply").value("Xin chào! Tôi là trợ lý AI."));
  }

  @Test
  @DisplayName("Demo: WireMock stub for Stripe payment")
  void demo_stubStripePaymentIntent() {
    // GIVEN - Stub Stripe create payment intent
    wireMockServer.stubFor(
        post(urlEqualTo("/v1/payment_intents"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        {
                          "id": "pi_test_123456",
                          "object": "payment_intent",
                          "amount": 50000,
                          "currency": "vnd",
                          "status": "requires_payment_method",
                          "client_secret": "pi_test_123456_secret_abc"
                        }
                        """)));

    // ℹ️ Similarly, call your payment creation endpoint and verify the result
  }
}
