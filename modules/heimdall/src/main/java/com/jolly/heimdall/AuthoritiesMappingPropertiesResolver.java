package com.jolly.heimdall;

import com.jolly.heimdall.properties.SimpleAuthoritiesMappingProperties;

import java.util.List;
import java.util.Map;

/**
 * @author jolly
 */
public interface AuthoritiesMappingPropertiesResolver {
  List<SimpleAuthoritiesMappingProperties> resolve(Map<String, Object> claimSet);
}
