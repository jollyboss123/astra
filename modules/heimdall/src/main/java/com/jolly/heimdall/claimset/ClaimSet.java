package com.jolly.heimdall.claimset;

import com.jayway.jsonpath.JsonPath;

import java.io.Serializable;
import java.util.Map;

/**
 * @author jolly
 */
public interface ClaimSet extends Map<String, Object>, Serializable {
  default <T> T getByJsonPath(String jsonPath) {
    return JsonPath.read(this, jsonPath);
  }
}
