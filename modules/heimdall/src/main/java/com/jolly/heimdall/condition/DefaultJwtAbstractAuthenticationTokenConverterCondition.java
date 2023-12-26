package com.jolly.heimdall.condition;

import com.jolly.heimdall.JwtAbstractAuthenticationTokenConverter;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Conditional;

/**
 * @author jolly
 */
public class DefaultJwtAbstractAuthenticationTokenConverterCondition extends AllNestedConditions {
  public DefaultJwtAbstractAuthenticationTokenConverterCondition() {
    super(ConfigurationPhase.REGISTER_BEAN);
  }

  @Conditional(IsOidcResourceServerCondition.class)
  static class HeimdallOidcResourceServerEnabled {}

  @ConditionalOnMissingBean(JwtAbstractAuthenticationTokenConverter.class)
  static class CustomAuthenticationConverterNotProvided {}
}
