package com.spring.backend.helper;

import com.spring.backend.configuration.user_details.UserDetailsCustom;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserHelper {
  public Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) return null;

    Object principal = auth.getPrincipal();
    if (principal instanceof UserDetailsCustom p) {
      return p.getId();
    }
    return null;
  }
}
