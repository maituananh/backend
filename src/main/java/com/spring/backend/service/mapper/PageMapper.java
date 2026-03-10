package com.spring.backend.service.mapper;

import com.spring.backend.dto.page.Pagination;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageMapper {
  public static <T, I> Pagination<T> toPagination(final Page<I> page, final List<T> data) {
    Pagination<T> pagination = new Pagination<>();
    pagination.setData(data);
    pagination.setTotalPages(page.getTotalPages());
    pagination.setTotalElements(page.getTotalElements());
    pagination.setCurrentPage(page.getPageable().getPageNumber() + 1);

    return pagination;
  }

  public static Pageable getPageable(final Integer page, final Integer size) {
    return PageRequest.of(
        (page == null || page == 0) ? 0 : page - 1, size == null || size <= 0 ? 10 : size);
  }

  public static Pageable getPageable(
      final Integer page, final Integer size, org.springframework.data.domain.Sort sort) {
    return PageRequest.of(
        (page == null || page == 0) ? 0 : page - 1, size == null || size <= 0 ? 10 : size, sort);
  }
}
