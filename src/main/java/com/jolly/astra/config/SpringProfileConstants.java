package com.jolly.astra.config;

/**
 * @author jolly
 */
public class SpringProfileConstants {
  public static final String SPRING_PROFILE_DEVELOPMENT = "dev";
  public static final String SPRING_PROFILE_TEST = "test";
  public static final String SPRING_PROFILE_E2E = "e2e";
  public static final String SPRING_PROFILE_PRODUCTION = "prod";
  public static final String SPRING_PROFILE_CLOUD = "cloud";
  public static final String SPRING_PROFILE_NO_FLYWAY = "no-flyway";
  public static final String SPRING_PROFILE_K8S = "k8s";

  private SpringProfileConstants() {}
}
