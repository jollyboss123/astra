package com.jolly.heimdalltest;

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.*;
import java.security.Principal;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

/**
 * @author jolly
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithMockAuthentication.Factory.class)
public @interface WithMockAuthentication {
  @AliasFor("value")
  String[] authorities() default {};

  @AliasFor("authorities")
  String[] value() default {};

  Class<? extends Authentication> authType() default Authentication.class;

  Class<?> principalType() default Principal.class;

  String name() default "user";

  @AliasFor(annotation = WithSecurityContext.class)
  TestExecutionEvent setupBefore() default TestExecutionEvent.TEST_METHOD;

  final class Factory implements WithSecurityContextFactory<WithMockAuthentication> {

    @Override
    public SecurityContext createSecurityContext(WithMockAuthentication annotation) {
      final SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication(annotation));
      return context;
    }

    public Authentication authentication(WithMockAuthentication annotation) {
      return new MockAuthenticationBuilder<>(
        annotation.authType(),
        mock(annotation.principalType())
      ).name(annotation.name())
        .authorities(Stream.concat(Stream.of(annotation.authorities()), Stream.of(annotation.value())).distinct())
        .build();
    }
  }
}
