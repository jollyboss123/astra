package com.jolly.heimdall.condition;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.authentication.AuthenticationManagerResolver;

/**
 * @author jolly
 */
public class DefaultAuthenticationManagerResolverCondition extends AllNestedConditions {
  DefaultAuthenticationManagerResolverCondition() {
    super(ConfigurationPhase.REGISTER_BEAN);
  }

  @ConditionalOnMissingBean(AuthenticationManagerResolver.class)
  static class CustomAuthenticationManagerResolverNotProvided {}
}
