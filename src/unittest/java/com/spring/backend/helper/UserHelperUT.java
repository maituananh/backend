package com.spring.backend.helper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.spring.backend.configuration.user_details.UserDetailsCustom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class UserHelperUT {

  private final UserHelper userHelper = new UserHelper();

  @Test
  @DisplayName("getCurrentUserId should return id if authenticated")
  void getCurrentUserId_Authenticated() {
    Authentication auth = mock(Authentication.class);
    UserDetailsCustom principal = mock(UserDetailsCustom.class);
    SecurityContext context = mock(SecurityContext.class);

    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getPrincipal()).thenReturn(principal);
    when(principal.getId()).thenReturn(10L);
    when(context.getAuthentication()).thenReturn(auth);

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
      mocked.when(SecurityContextHolder::getContext).thenReturn(context);

      Long id = userHelper.getCurrentUserId();
      assertThat(id).isEqualTo(10L);
    }
  }

  @Test
  @DisplayName("getCurrentUserId should return null if not authenticated")
  void getCurrentUserId_NotAuthenticated() {
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(null);

    try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
      mocked.when(SecurityContextHolder::getContext).thenReturn(context);

      assertThat(userHelper.getCurrentUserId()).isNull();
    }
  }
}
