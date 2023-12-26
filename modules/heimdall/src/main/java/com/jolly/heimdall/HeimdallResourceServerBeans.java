package com.jolly.heimdall;

import com.jolly.heimdall.condition.DefaultAuthenticationManagerResolverCondition;
import com.jolly.heimdall.condition.IsOidcResourceServerCondition;
import com.jolly.heimdall.hooks.ExpressionInterceptUrlRegistryPostProcessor;
import com.jolly.heimdall.hooks.ServerHttpSecurityPostProcessor;
import com.jolly.heimdall.properties.HeimdallOidcProperties;
import com.jolly.heimdall.properties.OpenIdProviderProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * All beans defined here are {@link ConditionalOnMissingBean}, define your own beans to override.
 *
 * @author jolly
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Conditional(IsOidcResourceServerCondition.class)
@EnableWebSecurity
@AutoConfiguration
@ImportAutoConfiguration(HeimdallOidcBeans.class)
public class HeimdallResourceServerBeans {
  private static final Logger log = LoggerFactory.getLogger(HeimdallResourceServerBeans.class);

  /**
   * Configures a {@link SecurityFilterChain} for a resource server with JwtDecoder with {@link Ordered#LOWEST_PRECEDENCE}. Defining a
   * {@link SecurityFilterChain} bean with no security matcher and an order higher than {@link Ordered#LOWEST_PRECEDENCE} will hide this
   * filter chain and disable most of heimdall autoconfiguration for OpenID resource servers.
   *
   * @param http HttpSecurity to configure
   * @param serverProperties Spring "server" configuration properties
   * @param heimdallProperties "com.jolly.heimdall" configuration properties
   * @param authorizePostProcessor Hook to override access control rules for all paths that are not listed in "permit-all"
   * @param httpPostProcessor Hook to override all or part of HttpSecurity autoconfiguration
   * @param authenticationManagerResolver Converts successful JWT decoding result into an {@link Authentication}
   * @param authenticationEntryPoint Spring oauth2 authentication entry point
   * @param accessDeniedHandler Spring oauth2 access denied handler
   * @return A {@link SecurityFilterChain} for servlet resource servers with JWT decoder.
   */
  @Order(Ordered.LOWEST_PRECEDENCE)
  @Bean
  SecurityFilterChain heimdallJwtResourceServerSecurityFilterChain(
    HttpSecurity http,
    ServerProperties serverProperties,
    HeimdallOidcProperties heimdallProperties,
    ExpressionInterceptUrlRegistryPostProcessor authorizePostProcessor,
    ServerHttpSecurityPostProcessor httpPostProcessor,
    AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver,
    AuthenticationEntryPoint authenticationEntryPoint,
    Optional<AccessDeniedHandler> accessDeniedHandler
  ) throws Exception {
    http.oauth2ResourceServer(oauth2 -> {
      oauth2.authenticationManagerResolver(authenticationManagerResolver);
      oauth2.authenticationEntryPoint(authenticationEntryPoint);
      accessDeniedHandler.ifPresent(oauth2::accessDeniedHandler);
    });

    ServletConfigurationUtils.configureResourceServer(http, serverProperties, heimdallProperties, authorizePostProcessor, httpPostProcessor);

    return http.build();
  }

  /**
   * Hook to override security rules for all paths that are not listed in "permit-all". Default is isAuthenticated().
   *
   * @return the hook.
   */
  @ConditionalOnMissingBean
  @Bean
  ExpressionInterceptUrlRegistryPostProcessor authorizePostProcessor() {
    return registry -> registry.anyRequest().authenticated();
  }

  /**
   * Hook to override all or part of {@link HttpSecurity} autoconfiguration. Called after heimdall configuration is applied so that you
   * can modify anything.
   *
   * @return the hook.
   */
  @ConditionalOnMissingBean
  @Bean
  ServerHttpSecurityPostProcessor httpPostProcessor() {
    return http -> http;
  }

  /**
   * Provides with multi-tenancy: builds a {@link AuthenticationManagerResolver<HttpServletRequest>} per provided OIDC issuer URI.
   *
   * @param oAuth2ResourceServerProperties "spring.security.oauth2.resourceserver" configuration properties
   * @param heimdallProperties "com.jolly.heimdall" configuration properties
   * @param jwtAuthenticationConverter converts a {@link Jwt} to {@link Authentication}
   * @return multi-tenant {@link AuthenticationManagerResolver<HttpServletRequest>} (one for each configured issuer)
   */
  @Conditional(DefaultAuthenticationManagerResolverCondition.class)
  @Bean
  AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(
    OAuth2ResourceServerProperties oAuth2ResourceServerProperties,
    HeimdallOidcProperties heimdallProperties,
    JwtAbstractAuthenticationTokenConverter jwtAuthenticationConverter
  ) {
    final OAuth2ResourceServerProperties.Jwt jwtProps = Optional.ofNullable(oAuth2ResourceServerProperties)
      .map(OAuth2ResourceServerProperties::getJwt)
      .orElse(null);

    if (jwtProps != null) {
      String uri;
      if (jwtProps.getIssuerUri() != null) {
        uri = jwtProps.getIssuerUri();
      } else {
        uri = jwtProps.getJwkSetUri();
      }

      if (StringUtils.hasLength(uri)) {
        log.warn("spring.security.oauth2.resourceserver configuration will be ignored in favour of com.jolly.heimdall");
      }
    }

    final Map<String, AuthenticationManager> jwtManagers = heimdallProperties.getOps().stream()
      .collect(Collectors.toMap(issuer -> issuer.getIss().toString(), issuer -> getAuthenticationManager(issuer, jwtAuthenticationConverter)));

    log.debug("building default JwtIssuerAuthenticationManagerResolver with: ",
              oAuth2ResourceServerProperties.getJwt(),
              heimdallProperties.getOps());

    return new JwtIssuerAuthenticationManagerResolver(jwtManagers::get);
  }

  @ConditionalOnMissingBean
  @Bean
  AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) -> {
      response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"Restricted Content\"");
      response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
    };
  }

  private static AuthenticationManager getAuthenticationManager(
    OpenIdProviderProperties op,
    JwtAbstractAuthenticationTokenConverter jwtAuthenticationConverter
  ) {
    final NimbusJwtDecoder decoder;
    if (op.getJwkSetUri() != null && StringUtils.hasLength(op.getJwkSetUri().toString())) {
      decoder = NimbusJwtDecoder.withJwkSetUri(op.getJwkSetUri().toString()).build();
    } else {
      decoder = NimbusJwtDecoder.withIssuerLocation(op.getIss().toString()).build();
    }

    final OAuth2TokenValidator<Jwt> defaultValidator;
    if (op.getIss() != null) {
      defaultValidator = JwtValidators.createDefaultWithIssuer(op.getIss().toString());
    } else {
      defaultValidator = JwtValidators.createDefault();
    }

    final OAuth2TokenValidator<Jwt> jwtValidator;
    if (op.getAud() != null && StringUtils.hasText(op.getAud())) {
      JwtClaimValidator<List<String>> audValidator = new JwtClaimValidator<>(
        JwtClaimNames.AUD,
        aud -> aud != null && aud.contains(op.getAud())
      );
      jwtValidator = new DelegatingOAuth2TokenValidator<>(List.of(defaultValidator, audValidator));
    } else {
      jwtValidator = defaultValidator;
    }

    decoder.setJwtValidator(jwtValidator);
    JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
    provider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
    return provider::authenticate;
  }
}
