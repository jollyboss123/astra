package com.jolly.astra.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

/**
 * @author jolly
 */
public class MockAuthenticationBuilder<A extends Authentication, T extends MockAuthenticationBuilder<A, T>> {
  private final A authMock;
  private static final String AUTH_NAME = "user";
  private static final List<String> AUTHORITIES = List.of();

  public MockAuthenticationBuilder(Class<A> authType, Object principal) {
    this(authType, principal, principal, principal);
  }

  public MockAuthenticationBuilder(Class<A> authType, Object principal, Object details, Object credentials) {
    this.authMock = mock(authType);
    name(AUTH_NAME);
    authorities(AUTHORITIES.stream());
    setAuthenticated(true);
    principal(principal);
    details(details);
    credentials(credentials);
  }

  public A build() {
    return authMock;
  }

  public T authorities(String... authorities) {
    return authorities(Stream.of(authorities));
  }

  public T authorities(Stream<String> authorities) {
    when(authMock.getAuthorities())
      .thenReturn((Collection) authorities.map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet()));
    return downcast();
  }

  public T name(String name) {
    when(authMock.getName())
      .thenReturn(name);
    return downcast();
  }

  public T credentials(Object credentials) {
    when(authMock.getCredentials())
      .thenReturn(credentials);
    return downcast();
  }

  public T details(Object details) {
    when(authMock.getDetails())
      .thenReturn(details);
    return downcast();
  }

  public T principal(Object principal) {
    when(authMock.getPrincipal())
      .thenReturn(principal);
    return downcast();
  }

  public T setAuthenticated(boolean authenticated) {
    when(authMock.isAuthenticated())
      .thenReturn(authenticated);
    return downcast();
  }

  public T configure(Consumer<A> authConsumer) {
    authConsumer.accept(authMock);
    return downcast();
  }

  protected T downcast() {
    return (T) this;
  }
}
