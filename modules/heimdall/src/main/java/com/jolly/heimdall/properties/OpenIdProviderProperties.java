package com.jolly.heimdall.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jolly
 */
public class OpenIdProviderProperties {
  /**
   * Must be exactly the same as in access tokens. In case of doubt, open one of your access tokens with a tool like
   * <a href="https://jwt.io">https://jwt.io</a>.
   */
  private URI iss;
  /**
   * Can be omitted if OpenID configuration can be retrieved from ${iss}/.well-known/openid-configuration.
   */
  private URI jwkSetUri;
  /**
   * Can be omitted. Will insert an audience validator if not null or empty.
   */
  private String aud;
  /**
   * Authorities mapping configuration, per claim.
   */
  @NestedConfigurationProperty
  private final List<SimpleAuthoritiesMappingProperties> authorities = new ArrayList<>();
  /**
   * JSON path for the claim to use as "name" source.
   */
  private String usernameClaim = StandardClaimNames.SUB;

  public URI getIss() {
    return iss;
  }

  public void setIss(URI iss) {
    this.iss = iss;
  }

  public URI getJwkSetUri() {
    return jwkSetUri;
  }

  public void setJwkSetUri(URI jwkSetUri) {
    this.jwkSetUri = jwkSetUri;
  }

  public String getAud() {
    return aud;
  }

  public void setAud(String aud) {
    this.aud = aud;
  }

  public List<SimpleAuthoritiesMappingProperties> getAuthorities() {
    return authorities;
  }

  public String getUsernameClaim() {
    return usernameClaim;
  }

  public void setUsernameClaim(String usernameClaim) {
    this.usernameClaim = usernameClaim;
  }
}
