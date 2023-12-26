package com.jolly.heimdall;

import com.jolly.heimdall.claimset.OpenIdClaimSet;
import com.jolly.heimdall.condition.DefaultJwtAbstractAuthenticationTokenConverterCondition;
import com.jolly.heimdall.properties.HeimdallOidcProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Map;

/**
 * @author jolly
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfiguration
@ImportAutoConfiguration(HeimdallOidcProperties.class)
public class HeimdallOidcBeans {
  private static final Logger log = LoggerFactory.getLogger(HeimdallOidcBeans.class);

  /**
   * Retrieves granted authorities from a claims-set (decoded from JWT or obtained from userinfo endpoint).
   *
   * @param heimdallProperties heimdall configuration properties
   * @return Portable converter to extract Spring-security authorities from OAuth 2.0 claims.
   */
  @ConditionalOnMissingBean
  @Bean
  ClaimSetAuthoritiesConverter authoritiesConverter(HeimdallOidcProperties heimdallProperties) {
    log.debug("building default ConfigurableClaimSetAuthoritiesConverter with: {}", heimdallProperties);
    return new ConfigurableClaimSetAuthoritiesConverter(heimdallProperties);
  }

  /**
   * Converter bean from {@link Jwt} to {@link AbstractAuthenticationToken}.
   *
   * @param authoritiesConverter converts access token claims into Spring authorities
   * @param heimdallProperties heimdall configuration properties
   * @return a converter from {@link Jwt} to {@link AbstractAuthenticationToken}
   */
  @Conditional(DefaultJwtAbstractAuthenticationTokenConverterCondition.class)
  @Bean
  JwtAbstractAuthenticationTokenConverter jwtAuthenticationConverter(
    Converter<Map<String, Object>, Collection<? extends GrantedAuthority>> authoritiesConverter,
    HeimdallOidcProperties heimdallProperties
  ) {
    return jwt -> new JwtAuthenticationToken(
      jwt,
      authoritiesConverter.convert(jwt.getClaims()),
      new OpenIdClaimSet(jwt.getClaims(), heimdallProperties.getOpProperties(jwt.getIssuer()).getUsernameClaim()).getName()
    );
  }
}
