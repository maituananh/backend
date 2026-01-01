package com.spring.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatisticProductByMonthDto {
  private int month;
  private long count;
}
