package com.spring.backend.dto.chat;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ProfileChatResponseDto extends ChatResponseDto<ProfileResultDto> {}
