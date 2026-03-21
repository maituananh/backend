package com.spring.backend.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Shared WireMock server configuration for integration tests.
 *
 * <p>WireMock simulates external HTTP APIs such as OpenAI, Stripe, AWS S3, etc., to allow testing
 * without depending on external services.
 *
 * <p>Uses a dynamic port to avoid {@code BindException: Address already in use} when multiple
 * Spring test ApplicationContexts are created (e.g. due to {@code @MockitoBean} producing different
 * context keys).
 *
 * <p>Usage in a test class:
 *
 * <pre>{@code
 * @Autowired
 * private WireMockServer wireMockServer;
 *
 * @BeforeEach
 * void setUp() {
 *     wireMockServer.resetAll();
 *     wireMockServer.stubFor(
 *         post(urlEqualTo("/v1/chat/completions"))
 *             .willReturn(aResponse()
 *                 .withStatus(200)
 *                 .withHeader("Content-Type", "application/json")
 *                 .withBody("""
 *                     { "choices": [{ "message": { "content": "mocked response" } }] }
 *                 """))
 *     );
 * }
 * }</pre>
 */
@TestConfiguration
@Profile("test")
public class WireMockConfig {

  @Bean(destroyMethod = "stop")
  public WireMockServer wireMockServer() {
    WireMockServer server =
        new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    // Start eagerly so we can read the dynamic port
    server.start();
    // Publish the port as a system property so application-test.yml
    // can resolve ${wiremock.server.port} in base-url / endpoint configs
    System.setProperty("wiremock.server.port", String.valueOf(server.port()));
    return server;
  }
}
