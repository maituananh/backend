package com.spring.backend.job;

import com.spring.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CronJobService {

  private final ProductService productService;

  @Recurring(id = "mark-product-status-progress", cron = "0 0 22 * * *")
  @Job(name = "mark-product-status-progress", retries = 0)
  public void updateStatusProduct() {
    productService.updateStatusIsProgress();
    productService.updateStatusIsExpired();
  }
}
