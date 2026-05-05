package com.spring.backend.infrastructure;

import com.spring.backend.config.IntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Smoke test: Spring context loads with all 7 domain repository adapters wired. Covers INFRA-01
 * integration requirement. Filled in after Plan 09-04 creates all RepositoryAdapter classes.
 */
@IntegrationTest
@Disabled("Stub — all 7 RepositoryAdapter classes must exist before this test can run (Plan 09-04)")
class InfrastructureContextIT {

  @Test
  @Disabled("Stub — implemented after all adapters created in Plan 09-04")
  void contextLoads_allDomainRepositoryAdaptersAreWired() {
    // TODO: @Autowired ProductJpaRepository productRepo (domain interface)
    // TODO: @Autowired CartJpaRepository cartRepo
    // TODO: @Autowired OrderJpaRepository orderRepo
    // then: all are non-null (adapter beans resolved)
  }
}
