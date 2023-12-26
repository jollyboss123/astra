package com.jolly.heimdall;

import com.jolly.heimdall.hooks.ExpressionInterceptUrlRegistryPostProcessor;
import com.jolly.heimdall.hooks.ServerHttpSecurityPostProcessor;
import com.jolly.heimdall.properties.Csrf;
import com.jolly.heimdall.properties.HeimdallOidcProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author jolly
 */
public class ServletConfigurationUtils {
  private ServletConfigurationUtils() {}

  public static HttpSecurity configureResourceServer(
    HttpSecurity http,
    ServerProperties serverProperties,
    HeimdallOidcProperties heimdallProperties,
    ExpressionInterceptUrlRegistryPostProcessor authorizePostProcessor,
    ServerHttpSecurityPostProcessor httpPostProcessor
  ) throws Exception {
    configureCors(http, heimdallProperties.getResourceServer().getCors());
    configureState(http, heimdallProperties.getResourceServer().isStatelessSession(), heimdallProperties.getResourceServer().getCsrf());
    configureAccess(http, heimdallProperties.getResourceServer().getPermitAll(), authorizePostProcessor);

    if (serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled()) {
      http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
    }

    return httpPostProcessor.process(http);
  }

  private static void configureCors(HttpSecurity http, List<HeimdallOidcProperties.ResourceServer.Cors> corsProps) throws Exception {
    if (corsProps.isEmpty()) {
      http.cors(cors -> cors.disable());
    } else {
      final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      for (final var corsProp : corsProps) {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(corsProp.getAllowedCredentials());
        configuration.setAllowedHeaders(corsProp.getAllowedHeaders());
        configuration.setAllowedMethods(corsProp.getAllowedMethods());
        configuration.setAllowedOriginPatterns(corsProp.getAllowedOriginPatterns());
        configuration.setExposedHeaders(corsProp.getExposedHeaders());
        configuration.setMaxAge(corsProp.getMaxAge());
        source.registerCorsConfiguration(corsProp.getPath(), configuration);
      }
    }
  }

  private static void configureState(HttpSecurity http, boolean isStateless, Csrf csrf) throws Exception {
    if (isStateless) {
      http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    http.csrf(configurer -> {
      switch (csrf) {
        case DISABLE -> configurer.disable();
        case DEFAULT -> {
          if (isStateless) {
            configurer.disable();
          }
        }
        case COOKIE_SESSION_FROM_JS -> {
          // Taken from
          // https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa-configuration
          configurer.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler());
          http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
        }
      }
    });
  }

  private static void configureAccess(HttpSecurity http, List<String> permitAll, ExpressionInterceptUrlRegistryPostProcessor authorizePostProcessor)
    throws Exception {
    if (!permitAll.isEmpty()) {
      http.anonymous(Customizer.withDefaults());
      http.authorizeHttpRequests(registry ->
        authorizePostProcessor.authorizeHttpRequests(
          registry.requestMatchers(permitAll.stream()
                                     .map(AntPathRequestMatcher::new)
                                     .toArray(AntPathRequestMatcher[]::new))
            .permitAll())
        );
    } else {
      http.authorizeHttpRequests(authorizePostProcessor::authorizeHttpRequests);
    }
  }

  /**
   * Copied from
   * <a href="https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa-configuration"/>
   */
  static final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
    private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> deferredCsrfToken) {
      /*
       * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of the CsrfToken when it is rendered in the response body.
       */
      this.delegate.handle(request, response, deferredCsrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
      /*
       * If the request contains a request header, use CsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies when a
       * single-page application includes the header value automatically, which was obtained via a cookie containing the raw CsrfToken.
       */
      if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
        return super.resolveCsrfTokenValue(request, csrfToken);
      }

      /*
       * In all other cases (e.g. if the request contains a request parameter), use XorCsrfTokenRequestAttributeHandler to resolve
       * the CsrfToken. This applies when a server-side rendered form includes the _csrf request parameter as a hidden input.
       */
      return this.delegate.resolveCsrfTokenValue(request, csrfToken);
    }
  }

  /**
   * Copied from
   * <a href="https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa-configuration"/>
   */
  static final class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
      CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
      // Render the token value to a cookie by causing the deferred token to be loaded
      csrfToken.getToken();

      filterChain.doFilter(request, response);
    }
  }
}
