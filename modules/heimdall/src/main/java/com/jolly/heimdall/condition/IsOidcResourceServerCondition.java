package com.jolly.heimdall.condition;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.server.resource.web.HeaderBearerTokenResolver;

/**
 * @author jolly
 */
public class IsOidcResourceServerCondition extends AllNestedConditions {
  public IsOidcResourceServerCondition() {
    super(ConfigurationPhase.PARSE_CONFIGURATION);
  }

  @ConditionalOnProperty(prefix = "com.jolly.heimdall.oidc.resourceserver", name = "enabled", matchIfMissing = true)
  static class HeimdallResourceServerEnabled {}

  @ConditionalOnClass(HeaderBearerTokenResolver.class)
  static class BearerTokenAuthenticationFilterIsOnClassPath {}
}
