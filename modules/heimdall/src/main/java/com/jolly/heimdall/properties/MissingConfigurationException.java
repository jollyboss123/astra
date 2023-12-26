package com.jolly.heimdall.properties;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * @author jolly
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class MissingConfigurationException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 3885551315358899174L;

  public MissingConfigurationException(String jwtIssuer) {
    super("check application properties: %s is not a trusted issuer".formatted(jwtIssuer));
  }
}
