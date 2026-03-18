package com.spring.backend.dto.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateAccountChatResponseDto extends ChatResponseDto<CreateAccountResultDto> {}
