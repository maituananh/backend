package com.spring.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponseDto {
  private long numberOfProduct;
  private long numberOfUser;
  private long numberOfUserActive;
}
