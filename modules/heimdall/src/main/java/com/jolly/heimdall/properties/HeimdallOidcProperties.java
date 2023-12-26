package com.jolly.heimdall.properties;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jolly
 */
@AutoConfiguration
@ConfigurationProperties(prefix = "com.jolly.heimdall.oidc")
public class HeimdallOidcProperties {
  @NestedConfigurationProperty
  private final List<OpenIdProviderProperties> ops = new ArrayList<>();
  private final ResourceServer resourceServer = new ResourceServer();

  public List<OpenIdProviderProperties> getOps() {
    return ops;
  }

  public ResourceServer getResourceServer() {
    return resourceServer;
  }

  public static class ResourceServer {
    private boolean enabled = true;
    private List<String> permitAll = new ArrayList<>();
    private boolean statelessSession = true;
    private Csrf csrf = Csrf.DISABLE;
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
      private String path = "/**";
      private Boolean allowedCredentials = null;
      private List<String> allowedOriginPatterns = List.of("*");
      private List<String> allowedMethods = List.of("*");
      private List<String> allowedHeaders = List.of("*");
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

  public OpenIdProviderProperties getOpProperties(String iss) throws MissingConfigurationException {
    for (final var op : getOps()) {
      String opIss = null;
      if (op.getIss() != null) {
        opIss = op.getIss().toString();
      }

      if (Objects.equals(opIss, iss)) {
        return op;
      }
    }

    throw new MissingConfigurationException(iss);
  }

  public OpenIdProviderProperties getOpProperties(Object iss) throws MissingConfigurationException {
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
