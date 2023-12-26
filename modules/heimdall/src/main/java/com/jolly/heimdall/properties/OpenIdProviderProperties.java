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
  private URI iss;
  private URI jwkSetUri;
  private String aud;
  @NestedConfigurationProperty
  private final List<SimpleAuthoritiesMappingProperties> authorities = new ArrayList<>();
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
