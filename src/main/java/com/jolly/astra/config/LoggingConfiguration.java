package com.jolly.astra.config;

import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jolly.astra.logging.LoggingUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jolly
 */
@Configuration
@RefreshScope
public class LoggingConfiguration {
  public LoggingConfiguration(
    @Value("${spring.application.name}") String appName,
    @Value("${server.port}") String serverPort,
    ApplicationProperties applicationProperties,
    ObjectProvider<BuildProperties> buildProperties,
    ObjectMapper mapper
  ) throws JsonProcessingException {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    Map<String, String> map = new HashMap<>();
    map.put("app_name", appName);
    map.put("app_port", serverPort);
    buildProperties.ifAvailable(it -> map.put("version", it.getVersion()));
    String customFields = mapper.writeValueAsString(map);

    ApplicationProperties.Logging loggingProperties = applicationProperties.getLogging();
    ApplicationProperties.Logging.Logstash logstashProperties = loggingProperties.getLogstash();

    if (loggingProperties.isUseJsonFormat()) {
      LoggingUtils.addJsonConsoleAppender(context, customFields);
    }
    if (logstashProperties.isEnabled()) {
      LoggingUtils.addLogstashTcpSocketAppender(context, customFields, logstashProperties);
    }
    if (loggingProperties.isUseJsonFormat() || logstashProperties.isEnabled()) {
      LoggingUtils.addContextListener(context, customFields, loggingProperties);
    }
  }
}
