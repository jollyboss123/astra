package com.jolly.astra.security.oauth2;

import com.jayway.jsonpath.JsonPath;
import com.jolly.astra.config.ApplicationProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author jolly
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {
  private final ApplicationProperties props;

  public JwtAuthenticationConverter(ApplicationProperties props) {
    this.props = props;
  }

  @Override
  public JwtAuthenticationToken convert(Jwt jwt) {
    final ApplicationProperties.Security.Issuer issuerProps = props.get(jwt.getIssuer());
    final Collection<? extends GrantedAuthority> authorities = new JwtGrantedAuthoritiesConverter(issuerProps).convert(jwt);
    final String username = JsonPath.read(jwt.getClaims(), issuerProps.getUsernameJsonPath());
    return new JwtAuthenticationToken(jwt, authorities, username);
  }
}
