package com.spring.backend.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Aggregated annotation to simplify writing integration tests.
 *
 * <p>Instead of declaring multiple annotations in each test class, simply use
 * {@code @IntegrationTest}.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @IntegrationTest
 * class UserControllerIT {
 *
 *     @Autowired
 *     MockMvc mockMvc;
 *
 *     @Autowired
 *     WireMockServer wireMockServer;
 *
 *     @BeforeEach
 *     void setUp() {
 *         wireMockServer.resetAll();
 *     }
 *
 *     @Test
 *     void shouldReturnUsers() throws Exception {
 *         mockMvc.perform(get("/api/v1/users")
 *                 .header("Authorization", "Bearer <token>"))
 *             .andExpect(status().isOk());
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({WireMockConfig.class, EmbeddedRedisConfig.class})
public @interface IntegrationTest {}
