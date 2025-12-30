package com.spring.backend.dto.page;

import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Pagination<T> {
  private List<T> data;
  private Integer totalPages;
  private Long totalElements;
  private Integer currentPage;
}
