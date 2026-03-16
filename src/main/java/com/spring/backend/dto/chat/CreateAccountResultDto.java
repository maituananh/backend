package com.spring.backend.dto.chat;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountResultDto {
  private Map<String, String> extracted;
  private List<String> missingFields;
  private String username;
  private String password;
}
