package com.jolly.heimdall;

import com.jolly.heimdall.claimset.OpenIdClaimSet;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @param <T> {@link OpenIdClaimSet} or any specialization.
 *
 * @author jolly
 */
public class OAuthentication<T extends Map<String, Object> & Serializable & Principal> extends AbstractAuthenticationToken implements
  OAuth2AuthenticatedPrincipal {
  @Serial
  private static final long serialVersionUID = 7566213493599769417L;

  /**
   * Bearer string to set as Authorization header if we ever need to call a downstream service on behalf of the same resource-owner.
   */
  private final String tokenString;
  /**
   * Claim-set associated with the access-token (attributes retrieved from the token or introspection end-point).
   */
  private final T claims;

  /**
   * @param claims      Claim-set of any-type
   * @param authorities Granted authorities associated with this authentication instance
   * @param tokenString Original encoded Bearer string (in case resource-server needs)
   */
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

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;
    OAuthentication<?> that = (OAuthentication<?>) object;
    return Objects.equals(tokenString, that.tokenString) && Objects.equals(claims, that.claims);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), tokenString, claims);
  }
}
