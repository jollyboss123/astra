package com.jolly.astra.config;

import java.io.Serial;

/**
 * @author jolly
 */
public class MisconfigurationException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 5031221909579344600L;

  public MisconfigurationException(String msg) {
    super(msg);
  }
}
