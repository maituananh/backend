package com.spring.backend.controller.dashboard;

import com.spring.backend.dto.dashboard.DashboardResponseDto;
import com.spring.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/statistic")
  public DashboardResponseDto getStatistic() {
    return dashboardService.getStatistic();
  }
}
