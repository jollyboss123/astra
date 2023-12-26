package com.jolly.astra.config;

import com.jolly.astra.security.AuthoritiesConstants;
import com.jolly.heimdall.JwtAbstractAuthenticationTokenConverter;
import com.jolly.heimdall.OAuthentication;
import com.jolly.heimdall.claimset.OpenIdClaimSet;
import com.jolly.heimdall.hooks.ExpressionInterceptUrlRegistryPostProcessor;
import com.jolly.heimdall.properties.HeimdallOidcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.Map;

/**
 * @author jolly
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
  @Bean
  JwtAbstractAuthenticationTokenConverter authenticationConverter(
    Converter<Map<String, Object>, Collection<? extends GrantedAuthority>> authoritiesConverter,
    HeimdallOidcProperties heimdallProperties
  ) {
    return jwt -> new OAuthentication<>(
      new OpenIdClaimSet(jwt.getClaims(), heimdallProperties.getOpProperties(jwt.getClaims().get(JwtClaimNames.ISS)).getUsernameClaim()),
      authoritiesConverter.convert(jwt.getClaims()),
      jwt.getTokenValue()
    );
  }

  @Bean
  ExpressionInterceptUrlRegistryPostProcessor expressionInterceptUrlRegistryPostProcessor() {
    return registry -> registry
      .requestMatchers(new AntPathRequestMatcher("/secured")).hasRole(AuthoritiesConstants.ADMIN)
      .anyRequest().authenticated();
  }
}
