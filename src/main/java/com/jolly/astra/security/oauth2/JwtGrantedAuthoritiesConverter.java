package com.jolly.astra.security.oauth2;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jolly.astra.config.ApplicationProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @author jolly
 */
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<? extends GrantedAuthority>> {
  private final ApplicationProperties.Security.Issuer props;

  public JwtGrantedAuthoritiesConverter(ApplicationProperties.Security.Issuer props) {
    this.props = props;
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Collection<? extends GrantedAuthority> convert(@NonNull Jwt jwt) {
    return props.getClaims().stream()
      .flatMap(claimProps -> {
        Object claim;
        try {
          claim = JsonPath.read(jwt.getClaims(), claimProps.getJsonPath());
        } catch (PathNotFoundException ex) {
          claim = null;
        }

        if (claim == null) {
          return Stream.empty();
        }

        if (claim instanceof String claimStr) {
          return Stream.of(claimStr.split(","));
        }

        if (claim instanceof String[] claimArr) {
          return Stream.of(claimArr);
        }

        if (Collection.class.isAssignableFrom(claim.getClass())) {
          final Iterator iter = ((Collection) claim).iterator();
          if (!iter.hasNext()) {
            return Stream.empty();
          }
          final Object firstItem = iter.next();
          if (firstItem instanceof String) {
            return (Stream<String>) ((Collection) claim).stream();
          }
          if (Collection.class.isAssignableFrom(firstItem.getClass())) {
            return (Stream<String>) ((Collection) claim).stream()
              .flatMap(colItem -> ((Collection) colItem).stream())
              .map(String.class::cast);
          }
        }

        return Stream.empty();
      }).map(SimpleGrantedAuthority::new)
      .map(GrantedAuthority.class::cast)
      .toList();
  }
}
