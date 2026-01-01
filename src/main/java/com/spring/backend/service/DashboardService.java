package com.spring.backend.service;

import com.spring.backend.dto.dashboard.DashboardResponseDto;
import com.spring.backend.dto.dashboard.StatisticProductByMonthDto;
import com.spring.backend.repository.ProductRepository;
import com.spring.backend.repository.UserRepository;
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

  public List<StatisticProductByMonthDto> statisticProductsByCurrentYear(int year) {

    List<StatisticProductByMonthDto> data = productRepository.statisticProductByMonth(year);

    Map<Integer, Long> map = new HashMap<>();
    data.forEach(d -> map.put(d.getMonth(), d.getCount()));

    List<StatisticProductByMonthDto> result = new ArrayList<>();
    for (int month = 1; month <= 12; month++) {
      result.add(new StatisticProductByMonthDto(month, map.getOrDefault(month, 0L)));
    }

    return result;
  }
}
