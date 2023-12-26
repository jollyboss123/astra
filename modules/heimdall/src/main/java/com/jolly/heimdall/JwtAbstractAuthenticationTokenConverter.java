package com.jolly.heimdall;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * @author jolly
 */
public interface JwtAbstractAuthenticationTokenConverter extends Converter<Jwt, AbstractAuthenticationToken> {
}
