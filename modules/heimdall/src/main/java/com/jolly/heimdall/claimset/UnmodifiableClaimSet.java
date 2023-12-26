package com.jolly.heimdall.claimset;

import java.io.Serial;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jolly
 */
public class UnmodifiableClaimSet extends DelegatingMap<String, Object> implements ClaimSet {
  @Serial
  private static final long serialVersionUID = -746179077627953070L;

  public UnmodifiableClaimSet(Map<String, Object> delegate) {
    super(Map.copyOf(delegate));
  }

  @Override
  public String toString() {
    return this.entrySet().stream()
      .map(e -> String.format("%s => %s", e.getKey(), e.getValue()))
      .collect(Collectors.joining(", ", "[", "]"));
  }
}
