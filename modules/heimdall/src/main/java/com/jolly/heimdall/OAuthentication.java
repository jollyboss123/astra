package com.jolly.heimdall;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @author jolly
 */
public class OAuthentication<T extends Map<String, Object> & Serializable & Principal> extends AbstractAuthenticationToken implements
  OAuth2AuthenticatedPrincipal {
  @Serial
  private static final long serialVersionUID = 7566213493599769417L;

  private final String tokenString;
  private final T claims;

  public OAuthentication(T claims, Collection<? extends GrantedAuthority> authorities, String tokenString) {
    super(authorities);
    super.setAuthenticated(true);
    super.setDetails(claims);
    this.claims = claims;
    this.tokenString = Optional.ofNullable(tokenString)
      .map(t -> t.toLowerCase().startsWith("bearer ") ? t.substring(7) : t)
      .orElse(null);
  }

  @Override
  public void setAuthenticated(boolean authenticated) {
    throw new RuntimeException("OAuthentication authentication status is immutable");
  }

  @Override
  public Object getCredentials() {
    return tokenString;
  }

  @Override
  public Object getPrincipal() {
    return claims;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return claims;
  }

  public T getClaims() {
    return claims;
  }

  public String getBearerHeader() {
    if (!StringUtils.hasText(tokenString)) {
      return null;
    }

    return String.format("Bearer %s", tokenString);
  }
}
