package com.jolly.heimdall.claimset;

import com.jayway.jsonpath.PathNotFoundException;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimAccessor;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.io.Serial;
import java.security.Principal;
import java.util.Map;

/**
 * @author jolly
 */
public class OpenIdClaimSet extends UnmodifiableClaimSet implements IdTokenClaimAccessor, Principal {
  @Serial
  private static final long serialVersionUID = 4908273025927451191L;

  /**
   * JSON path for the claim to use as "name" source
   */
  private final String usernameClaim;

  public OpenIdClaimSet(Map<String, Object> claims, String usernameClaim) {
    super(claims);
    this.usernameClaim = usernameClaim;
  }

  public OpenIdClaimSet(Map<String, Object> claims) {
    this(claims, StandardClaimNames.SUB);
  }

  @Override
  public String getName() {
    try {
      return getByJsonPath(usernameClaim);
    } catch (PathNotFoundException ex) {
      return getByJsonPath(JwtClaimNames.SUB);
    }
  }

  @Override
  public Map<String, Object> getClaims() {
    return this;
  }
}
