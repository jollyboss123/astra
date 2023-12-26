package com.jolly.astra;

import com.jolly.astra.config.ApplicationProperties;
import com.jolly.astra.config.CRLFLogConverter;
import com.jolly.astra.config.SpringProfileConstants;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class AstraApp {
  private static final Logger log = LoggerFactory.getLogger(AstraApp.class);
  private final Environment env;

  public AstraApp(Environment env) {
    this.env = env;
  }

  @PostConstruct
  public void initApplication() {
    Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
    if (
      activeProfiles.contains(SpringProfileConstants.SPRING_PROFILE_DEVELOPMENT) &&
        activeProfiles.contains(SpringProfileConstants.SPRING_PROFILE_PRODUCTION)
    ) {
      log.error(
        "You have misconfigured your application! It should not run with both the 'dev' and 'prod' profiles at the same time."
      );
    }
    if (
      activeProfiles.contains(SpringProfileConstants.SPRING_PROFILE_DEVELOPMENT) &&
        activeProfiles.contains(SpringProfileConstants.SPRING_PROFILE_CLOUD)
    ) {
      log.error(
        "You have misconfigured your application! It should not run with both the 'dev' and 'cloud' profiles at the same time."
      );
    }
  }

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(AstraApp.class);
    addDefaultProfile(app);
    Environment env = app.run(args).getEnvironment();
    logApplicationStartup(env);
  }

  private static void addDefaultProfile(SpringApplication app) {
    Map<String, Object> defProperties = new HashMap<>();
    defProperties.put("spring.profiles.default", SpringProfileConstants.SPRING_PROFILE_DEVELOPMENT);
    app.setDefaultProperties(defProperties);
  }

  private static void logApplicationStartup(Environment env) {
    String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
    String applicationName = env.getProperty("spring.application.name");
    String serverPort = env.getProperty("server.port");
    String contextPath = Optional
      .ofNullable(env.getProperty("server.servlet.context-path"))
      .filter(StringUtils::hasLength)
      .orElse("/");
    String hostAddress = "localhost";
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.warn("The host name could not be determined, using `localhost` as fallback");
    }
    log.info(
      CRLFLogConverter.CRLF_SAFE_MARKER,
      """

      ----------------------------------------------------------
      \tApplication '{}' is running! Access URLs:
      \tLocal: \t\t{}://localhost:{}{}
      \tExternal: \t{}://{}:{}{}
      \tProfile(s): \t{}
      ----------------------------------------------------------""",
      applicationName,
      protocol,
      serverPort,
      contextPath,
      protocol,
      hostAddress,
      serverPort,
      contextPath,
      env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles()
    );

    String configServerStatus = env.getProperty("configserver.status");
    if (configServerStatus == null) {
      configServerStatus = "Not found or not setup for this application";
    }
    log.info(
      CRLFLogConverter.CRLF_SAFE_MARKER,
      "\n----------------------------------------------------------\n\t" +
        "Config Server: \t{}\n----------------------------------------------------------",
      configServerStatus
    );
  }
}
