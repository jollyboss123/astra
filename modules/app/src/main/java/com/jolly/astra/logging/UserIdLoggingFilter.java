package com.jolly.astra.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author jolly
 */
public class UserIdLoggingFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
    @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null) {
        final String userId = auth.getName();
        MDC.put("userId", userId);
      }

      filterChain.doFilter(request, response);
    } finally {
      MDC.remove("userId");
    }
  }
}
