package com.jolly.heimdall;

import com.jolly.heimdall.claimset.OpenIdClaimSet;
import com.jolly.heimdall.properties.HeimdallOidcProperties;
import com.jolly.heimdall.properties.OpenIdProviderProperties;
import com.jolly.heimdall.properties.SimpleAuthoritiesMappingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.jolly.heimdall.properties.SimpleAuthoritiesMappingProperties.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jolly
 */
class ConfigurableJwtGrantedAuthoritiesConverterTest {

  @Test
  void test() throws URISyntaxException {
    final URI issuer = new URI("https://authorization-server");
    final List<String> client1Roles = List.of("R11", "r12");
    final List<String> client2Roles = List.of("R21", "r22");
    final List<String> client3Roles = List.of("R31", "r32");
    final List<String> realmRoles = List.of("r1", "r2");

    final Map<String, Object> claims = Map.of(
      JwtClaimNames.ISS, issuer,
      "resource_access", Map.of(
        "client1", Map.of("roles", client1Roles),
        "client2", Map.of("roles", String.join(", ", client2Roles)),
        "client3", Map.of("roles", String.join(" ", client3Roles))
      ),
      "realm_access", Map.of("roles", realmRoles)
    );

    final Instant now = Instant.now();
    final Jwt jwt = new Jwt("a.b.C", now, Instant.ofEpochSecond(now.getEpochSecond() + 3600), Map.of("machin", "truc"), claims);

    final OpenIdProviderProperties issuerProps = mock(OpenIdProviderProperties.class);
    issuerProps.setIss(issuer);

    final HeimdallOidcProperties props = mock(HeimdallOidcProperties.class);
    when(props.getOpProperties(issuer)).thenReturn(issuerProps);

    final ConfigurableClaimSetAuthoritiesConverter converter = new ConfigurableClaimSetAuthoritiesConverter(props);
    final OpenIdClaimSet claimSet = new OpenIdClaimSet(jwt.getClaims());

    // assert mapping with default properties
    final List<String> expectedDefault = List.of("r1", "r2");
    boolean defaultContains = converter.convert(claimSet).stream()
      .map(GrantedAuthority::getAuthority)
      .allMatch(expectedDefault::contains);
    assertTrue(defaultContains);

    // assert with prefix and case processing
    when(issuerProps.getAuthorities())
      .thenReturn(
        List.of(
          configureSimpleAuthorities("$.realm_access.roles", "MACHIN_", Case.UNCHANGED),
          configureSimpleAuthorities("resource_access.client1.roles", "TRUC_", Case.LOWER),
          configureSimpleAuthorities("resource_access.client3.roles", "CHOSE_", Case.UPPER)
        )
      );

    final List<String> expected = List.of("TRUC_r11", "TRUC_r12", "CHOSE_R31", "CHOSE_R32", "MACHIN_r1", "MACHIN_r2");
    boolean contains = converter.convert(claimSet).stream()
      .map(GrantedAuthority::getAuthority)
      .allMatch(expected::contains);
    assertTrue(contains);
  }

  private static SimpleAuthoritiesMappingProperties configureSimpleAuthorities(String jsonPath, String prefix, Case caseProcessing) {
    final SimpleAuthoritiesMappingProperties props = new SimpleAuthoritiesMappingProperties();
    props.setPath(jsonPath);
    props.setPrefix(prefix);
    props.setCaseProcessing(caseProcessing);

    return props;
  }
}
