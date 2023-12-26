package com.jolly.heimdall;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jolly.heimdall.properties.HeimdallOidcProperties;
import com.jolly.heimdall.properties.SimpleAuthoritiesMappingProperties;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Portable converter to extract Spring-security authorities from OAuth 2.0 claims.
 * <p>
 *   It is designed to work with {@link HeimdallOidcProperties} which enables to configure:
 * </p>
 * <ul>
 *   <li>source claims (which claims to pick authorities from)</li>
 *   <li>for each claim: prefix (should be anything like "ROLE_" or "SCOPE_" pre-pended to authorities)</li>
 *   <li>for each claim: case transformation (process defined casing for authorities)</li>
 * </ul>
 *
 * @author jolly
 */
public class ConfigurableClaimSetAuthoritiesConverter implements ClaimSetAuthoritiesConverter{
  private final AuthoritiesMappingPropertiesResolver authoritiesMappingPropertiesProvider;

  public ConfigurableClaimSetAuthoritiesConverter(HeimdallOidcProperties props) {
    this.authoritiesMappingPropertiesProvider = new ByIssuerAuthoritiesMappingPropertiesResolver(props);
  }

  @Override
  public Collection<? extends GrantedAuthority> convert(@NonNull Map<String, Object> source) {
    final List<SimpleAuthoritiesMappingProperties> authoritiesMappingProperties = authoritiesMappingPropertiesProvider.resolve(source);

    return authoritiesMappingProperties.stream()
      .flatMap(authoritiesMappingProps -> getAuthorities(source, authoritiesMappingProps))
      .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r))
      .toList();
  }

  private static Stream<String> getAuthorities(Map<String, Object> claims, SimpleAuthoritiesMappingProperties props) {
    return getClaims(claims, props.getPath())
      .flatMap(claim -> Stream.of(claim.split(",")))
      .flatMap(claim -> Stream.of(claim.split(" ")))
      .filter(StringUtils::hasText)
      .map(String::trim)
      .map(r -> processCase(r, props.getCaseProcessing()))
      .map(r -> String.format("%s%s", props.getPrefix(), r));
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static Stream<String> getClaims(Map<String, Object> claims, String path) {
    try {
      final Object res = JsonPath.read(claims, path);
      if (res instanceof String s) {
        return Stream.of(s);
      }
      if (res instanceof List<?> l) {
        if (l.isEmpty()) {
          return Stream.empty();
        }
        if (l.get(0) instanceof String) {
          return (Stream<String>) l.stream();
        }
        if (l.get(0) instanceof List) {
          return l.stream().flatMap(o -> ((List) o).stream());
        }
      }
      return Stream.empty();
    } catch (PathNotFoundException ex) {
      return Stream.empty();
    }
  }

  private static String processCase(String role, SimpleAuthoritiesMappingProperties.Case caseProps) {
    switch (caseProps) {
      case UPPER -> {
        return role.toUpperCase();
      }
      case LOWER -> {
        return role.toLowerCase();
      }
      default -> {
        return role;
      }
    }
  }
}
