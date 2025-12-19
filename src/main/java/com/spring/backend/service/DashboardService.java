package com.spring.backend.service;

import com.spring.backend.dto.dashboard.DashboardResponseDto;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  public DashboardResponseDto getStatistic() {
    long numberOfProduct = productRepository.count();
    long numberOfUser = userRepository.count();
    long numberOfUserActive = userRepository.countByIsActiveIsTrue();

    return new DashboardResponseDto(numberOfProduct, numberOfUser, numberOfUserActive);
  }
}
