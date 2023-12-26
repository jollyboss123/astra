package com.jolly.heimdall.hooks;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * @author jolly
 */
public interface ServerHttpSecurityPostProcessor {
  HttpSecurity process(HttpSecurity http) throws Exception;
}
