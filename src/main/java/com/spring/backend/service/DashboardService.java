package com.spring.backend.service;

import com.spring.backend.dto.dashboard.DashboardResponseDto;
import com.spring.backend.entity.ProductStatistic;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
import java.time.Year;
import java.util.*;
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

  public List<ProductStatistic> statisticProductsByCurrentYear() {

    int year = Year.now().getValue();

    List<ProductStatistic> data = productRepository.statisticProductByMonth(year);

    Map<Integer, Integer> map = new HashMap<>();
    data.forEach(d -> map.put(d.getMonth(), d.getProductCount()));

    List<ProductStatistic> result = new ArrayList<>();

    for (int month = 1; month <= 12; month++) {
      final int m = month;
      int count = map.getOrDefault(m, 0);

      result.add(
          new ProductStatistic() {
            @Override
            public int getMonth() {
              return m;
            }

            @Override
            public int getProductCount() {
              return count;
            }
          });
    }
    return result;
  }
}
