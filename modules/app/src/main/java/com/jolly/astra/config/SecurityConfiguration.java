package com.jolly.astra.config;

import com.jolly.astra.security.oauth2.JwtAuthenticationConverter;
import com.jolly.heimdall.JwtAbstractAuthenticationTokenConverter;
import com.jolly.heimdall.OAuthentication;
import com.jolly.heimdall.claimset.OpenIdClaimSet;
import com.jolly.heimdall.hooks.ExpressionInterceptUrlRegistryPostProcessor;
import com.jolly.heimdall.properties.HeimdallOidcProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
      .requestMatchers(new AntPathRequestMatcher("/secured")).hasRole("ADMIN")
      .anyRequest().authenticated();
  }

//  @Bean
//  public SecurityFilterChain filterChain(
//    HttpSecurity http,
//    ServerProperties serverProperties,
//    @Value("${origins:[]}") List<String> origins,
//    @Value("${permit-all:[]}") List<String> permitAll,
//    AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver
//    ) throws Exception {
//    http
//      .oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(authenticationManagerResolver))
//      .cors(cors -> cors.configurationSource(corsConfigurationSource(origins)))
//      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//      .csrf(csrf -> csrf.disable())
//      .exceptionHandling(eh ->
//        eh.authenticationEntryPoint(((request, response, authException) -> {
//          response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=\"Restricted Content\"");
//          response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
//        })
//        )
//      );
//
//    if (serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled()) {
//      http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
//    }
//
//    http.authorizeHttpRequests(requests ->
//      requests
//        .requestMatchers(permitAll.stream().map(AntPathRequestMatcher::new).toArray(AntPathRequestMatcher[]::new)).permitAll()
//        .anyRequest().authenticated()
//      );
//
//    return http.build();
//  }
//
//  private UrlBasedCorsConfigurationSource corsConfigurationSource(List<String> origins) {
//    final CorsConfiguration configuration = new CorsConfiguration();
//    configuration.setAllowedOrigins(origins);
//    configuration.setAllowedMethods(List.of("*"));
//    configuration.setAllowedHeaders(List.of("*"));
//    configuration.setExposedHeaders(List.of("*"));
//
//    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//    source.registerCorsConfiguration("/**", configuration);
//    return source;
//  }
//
//  @Bean
//  AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(
//    ApplicationProperties props,
//    JwtAuthenticationConverter authenticationConverter
//  ) {
//    final Map<String, AuthenticationManager> authenticationProviders = props.getSecurity().getIssuers().stream()
//      .map(ApplicationProperties.Security.Issuer::getUri)
//      .map(URL::toString)
//      .collect(Collectors.toMap(issuer -> issuer, issuer -> authenticationProvider(issuer, authenticationConverter)::authenticate));
//
//    return new JwtIssuerAuthenticationManagerResolver(authenticationProviders::get);
//  }
//
//  private JwtAuthenticationProvider authenticationProvider(String issuer, JwtAuthenticationConverter authenticationConverter) {
//    JwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
//    JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
//    provider.setJwtAuthenticationConverter(authenticationConverter);
//    return provider;
//  }
}
