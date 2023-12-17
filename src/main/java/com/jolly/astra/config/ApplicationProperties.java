package com.jolly.astra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jolly
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
  private final Logging logging = new Logging();

  public Logging getLogging() {
    return logging;
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
}
