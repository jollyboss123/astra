package com.jolly.astra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jolly
 */
@ConfigurationProperties(prefix = "astra", ignoreUnknownFields = false)
public class ApplicationProperties {
  private final Logging logging = new Logging();
  private final Security security = new Security();

  public Logging getLogging() {
    return logging;
  }

  public Security getSecurity() {
    return security;
  }

  public Security.Issuer get(URL issuerUri) throws MisconfigurationException {
    final List<Security.Issuer> issuerProperties = this.security.getIssuers().stream()
      .filter(iss -> Objects.equals(issuerUri, iss.getUri()))
      .toList();

    if (issuerProperties.isEmpty()) {
      throw new MisconfigurationException("missing authorities mapping properties for %s".formatted(issuerUri.toString()));
    }

    if (issuerProperties.size() > 1) {
      throw new MisconfigurationException("too many authorities mapping properties for %s".formatted(issuerUri.toString()));
    }

    return issuerProperties.get(0);
  }

  public static class Logging {
    private boolean useJsonFormat = false;
    private final Logstash logstash = new Logstash();

    public Logging() {}

    public boolean isUseJsonFormat() {
      return useJsonFormat;
    }

    public void setUseJsonFormat(boolean useJsonFormat) {
      this.useJsonFormat = useJsonFormat;
    }

    public Logstash getLogstash() {
      return logstash;
    }

    public static class Logstash {
      private boolean enabled = false;
      private String host = "localhost";
      private int port = 7000;
      private int ringBufferSize = 512;

      public Logstash() {}

      public boolean isEnabled() {
        return enabled;
      }

      public void setEnabled(boolean enabled) {
        this.enabled = enabled;
      }

      public String getHost() {
        return host;
      }

      public void setHost(String host) {
        this.host = host;
      }

      public int getPort() {
        return port;
      }

      public void setPort(int port) {
        this.port = port;
      }

      public int getRingBufferSize() {
        return ringBufferSize;
      }

      public void setRingBufferSize(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
      }
    }
  }

  public static class Security {
    private final List<Issuer> issuers = new ArrayList<>();

    public Security() {}

    public List<Issuer> getIssuers() {
      return issuers;
    }

    public static class Issuer {
      private URL uri;
      private final List<Claim> claims = new ArrayList<>();
      private String usernameJsonPath = JwtClaimNames.SUB;

      public Issuer() {}

      public URL getUri() {
        return uri;
      }

      public void setUri(URL uri) {
        this.uri = uri;
      }

      public List<Claim> getClaims() {
        return claims;
      }

      public String getUsernameJsonPath() {
        return usernameJsonPath;
      }

      public void setUsernameJsonPath(String usernameJsonPath) {
        this.usernameJsonPath = usernameJsonPath;
      }

      public static class Claim {
        private String jsonPath;
        private CaseProcessing caseProcessing = CaseProcessing.UNCHANGED;
        private String prefix = "";

        public Claim() {}

        public enum CaseProcessing {
          UNCHANGED, TO_LOWER, TO_UPPER
        }

        public String getJsonPath() {
          return jsonPath;
        }

        public void setJsonPath(String jsonPath) {
          this.jsonPath = jsonPath;
        }

        public CaseProcessing getCaseProcessing() {
          return caseProcessing;
        }

        public void setCaseProcessing(CaseProcessing caseProcessing) {
          this.caseProcessing = caseProcessing;
        }

        public String getPrefix() {
          return prefix;
        }

        public void setPrefix(String prefix) {
          this.prefix = prefix;
        }
      }
    }
  }
}
