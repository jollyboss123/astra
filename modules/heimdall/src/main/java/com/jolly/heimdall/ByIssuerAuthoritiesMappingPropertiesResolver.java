package com.jolly.heimdall;

import com.jolly.heimdall.properties.HeimdallOidcProperties;
import com.jolly.heimdall.properties.SimpleAuthoritiesMappingProperties;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.util.List;
import java.util.Map;

/**
 * @author jolly
 */
public class ByIssuerAuthoritiesMappingPropertiesResolver implements AuthoritiesMappingPropertiesResolver {
  private final HeimdallOidcProperties props;

  public ByIssuerAuthoritiesMappingPropertiesResolver(HeimdallOidcProperties props) {
    this.props = props;
  }

  @Override
  public List<SimpleAuthoritiesMappingProperties> resolve(Map<String, Object> claimSet) {
    Object iss = claimSet.get(JwtClaimNames.ISS);
    return props.getOpProperties(iss).getAuthorities();
  }
}
