package com.jolly.heimdall.hooks;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Customize access control for routes not defined in
 * {@link com.jolly.heimdall.properties.HeimdallOidcProperties.ResourceServer#permitAll}
 *
 * @author jolly
 */
public interface ExpressionInterceptUrlRegistryPostProcessor {
  AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorizeHttpRequests(
    AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
  );
}
