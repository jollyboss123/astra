package com.jolly.astra.config;

import com.jolly.astra.logging.LoggingAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * @author jolly
 */
@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {
  @Bean
  @Profile(SpringProfileConstants.SPRING_PROFILE_DEVELOPMENT)
  public LoggingAspect loggingAspect(Environment env) {
    return new LoggingAspect(env);
  }
}
