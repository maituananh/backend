package com.spring.backend.infrastructure.adapter;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ProductRepositoryAdapter — covers INFRA-01. Stubs filled in after Plan 09-04
 * creates the adapter class.
 */
class ProductRepositoryAdapterUT {

  @Test
  @Disabled("Stub — implemented after ProductRepositoryAdapter is created in Plan 09-04")
  void findById_delegatesToJpaRepositoryAndMapsResult() {
    // TODO: mock ProductJpaRepository and ProductMapper
    // given: jpaRepository.findById(1L) returns Optional.of(productEntity)
    //        mapper.toDomain(productEntity) returns domainProduct
    // when: adapter.findById(1L)
    // then: Optional containing domainProduct is returned
  }

  @Test
  @Disabled("Stub — implemented after ProductRepositoryAdapter is created in Plan 09-04")
  void findById_returnsEmptyWhenNotFound() {
    // TODO: jpaRepository.findById returns Optional.empty()
    // when: adapter.findById(99L)
    // then: Optional.empty() returned
  }
}
