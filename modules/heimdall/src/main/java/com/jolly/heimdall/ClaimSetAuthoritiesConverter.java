package com.jolly.heimdall;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

/**
 * @author jolly
 */
public interface ClaimSetAuthoritiesConverter extends Converter<Map<String, Object>, Collection<? extends GrantedAuthority>> {
}
