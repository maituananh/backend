package com.spring.backend.config;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

/**
 * Embedded Redis Server configuration for integration tests.
 *
 * <p>Embedded Redis starts before the Spring context loads and stops after tests complete, allowing
 * Redis testing without a real Redis installation.
 *
 * <p>Port 6370 is used to avoid conflicts with a real Redis instance (standard port 6379).
 */
@TestConfiguration
@Profile("test")
public class EmbeddedRedisConfig {

  private static final int REDIS_TEST_PORT = 6370;

  private RedisServer redisServer;

  @Bean
  public RedisServer embeddedRedisServer() throws IOException {
    var builder = RedisServer.builder().port(REDIS_TEST_PORT);
    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      builder.setting("maxheap 128M");
    }
    redisServer = builder.build();
    try {
      redisServer.start();
    } catch (Exception e) {
      // Port may already be bound by another test context's embedded Redis
      // This is fine - we'll reuse the existing instance
    }
    return redisServer;
  }

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration config =
        new RedisStandaloneConfiguration("localhost", REDIS_TEST_PORT);
    return new LettuceConnectionFactory(config);
  }

  @PreDestroy
  public void stopRedis() {
    if (redisServer != null && redisServer.isActive()) {
      redisServer.stop();
    }
  }
}
