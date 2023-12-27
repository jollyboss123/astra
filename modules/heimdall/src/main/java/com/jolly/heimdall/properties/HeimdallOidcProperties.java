package com.jolly.heimdall.properties;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Configuration properties for OAuth 2.0 autoconfiguration extensions to spring-boot-starter-oauth2-resource-server.
 *
 * @author jolly
 */
@AutoConfiguration
@ConfigurationProperties(prefix = "com.jolly.heimdall.oidc")
public class HeimdallOidcProperties {
  /**
   * OpenID Providers configuration: JWK set URI, issuer URI, audience, and authorities mapping configuration for each issuer. A minimum
   * of one issuer is required. <b>Properties defined here are a replacement for spring.security.oauth2.resourceserver.jwt.*</b> (which
   * will be ignored). Authorities mapping defined here will be used by the resource server filter chain.
   */
  @NestedConfigurationProperty
  private final List<OpenIdProviderProperties> ops = new ArrayList<>();
  /**
   * Autoconfiguration for an OAuth 2.0 resource server {@link SecurityFilterChain} with
   * {@link Ordered#LOWEST_PRECEDENCE}. <p>Default configuration:
   * <ul>
   *   <li>no {@link HttpSecurity#securityMatcher(String...)} to process
   *   all the requests that were not intercepted by higher {@link Ordered} {@link SecurityFilterChain}</li>
   *   <li>no session</li>
   *   <li>disabled CSRF protection</li>
   *   <li>401 to unauthorized requests</li>
   * </ul>
   */
  private final ResourceServer resourceServer = new ResourceServer();

  public List<OpenIdProviderProperties> getOps() {
    return ops;
  }

  public ResourceServer getResourceServer() {
    return resourceServer;
  }

  public static class ResourceServer {
    /**
     * If true, instantiate resource server {@link SecurityFilterChain} bean and all its dependencies
     */
    private boolean enabled = true;
    /**
     * Path matchers for the routes accessible to anonymous requests.
     */
    private List<String> permitAll = new ArrayList<>();
    private boolean statelessSession = true;
    private Csrf csrf = Csrf.DISABLE;
    /**
     * Fine-grained CORS configuration.
     */
    private final List<Cors> cors = new ArrayList<>();

    public ResourceServer() {}

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public List<String> getPermitAll() {
      return permitAll;
    }

    public void setPermitAll(List<String> permitAll) {
      this.permitAll = permitAll;
    }

    public boolean isStatelessSession() {
      return statelessSession;
    }

    public void setStatelessSession(boolean statelessSession) {
      this.statelessSession = statelessSession;
    }

    public Csrf getCsrf() {
      return csrf;
    }

    public void setCsrf(Csrf csrf) {
      this.csrf = csrf;
    }

    public List<Cors> getCors() {
      return cors;
    }

    public static class Cors {
      /**
       * Path matcher to which this configuration entry applies.
       */
      private String path = "/**";
      private Boolean allowedCredentials = null;
      /**
       * Default is "*" which allows all origins
       */
      private List<String> allowedOriginPatterns = List.of("*");
      /**
       * Default is "*" which allows all methods
       */
      private List<String> allowedMethods = List.of("*");
      /**
       * Default is "*" which allows all headers
       */
      private List<String> allowedHeaders = List.of("*");
      /**
       * Default is "*" which exposes all origins
       */
      private List<String> exposedHeaders = List.of("*");
      private Long maxAge = null;

      public Cors() {}

      public String getPath() {
        return path;
      }

      public void setPath(String path) {
        this.path = path;
      }

      public Boolean getAllowedCredentials() {
        return allowedCredentials;
      }

      public void setAllowedCredentials(Boolean allowedCredentials) {
        this.allowedCredentials = allowedCredentials;
      }

      public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
      }

      public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
      }

      public List<String> getAllowedMethods() {
        return allowedMethods;
      }

      public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
      }

      public List<String> getAllowedHeaders() {
        return allowedHeaders;
      }

      public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
      }

      public List<String> getExposedHeaders() {
        return exposedHeaders;
      }

      public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
      }

      public Long getMaxAge() {
        return maxAge;
      }

      public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
      }
    }
  }

  /**
   * @param iss the issuer URI string
   * @return configuration properties associated with the provided issuer URI
   * @throws MisconfigurationException if configuration properties do not have an entry of the exact issuer
   */
  public OpenIdProviderProperties getOpProperties(String iss) throws MisconfigurationException {
    for (final var op : getOps()) {
      String opIss = null;
      if (op.getIss() != null) {
        opIss = op.getIss().toString();
      }

      if (Objects.equals(opIss, iss)) {
        return op;
      }
    }

    throw new MisconfigurationException(iss);
  }

  /**
   * @param iss the issuer URL
   * @return configuration properties associated with the provided issuer URI
   * @throws MisconfigurationException if configuration properties do not have an entry of the exact issuer
   */
  public OpenIdProviderProperties getOpProperties(Object iss) throws MisconfigurationException {
    if (iss == null && getOps().size() == 1) {
      return getOps().get(0);
    }

    String issStr = null;
    if (iss != null) {
      issStr = iss.toString();
    }

    return getOpProperties(issStr);
  }
}
