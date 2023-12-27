package com.jolly.astra;

import com.jolly.heimdall.OAuthentication;
import com.jolly.heimdall.claimset.OpenIdClaimSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {
  private static final Logger log = LoggerFactory.getLogger(GreetingController.class);

  @GetMapping("/greet")
  @PreAuthorize("isAuthenticated()")
  public String greet(OAuthentication<OpenIdClaimSet> auth) {
    log.info("greeting");
    return String.format("Hi %s! You are granted with %s.", auth.getName(), auth.getAuthorities());
  }

	@GetMapping("/restricted")
  @PreAuthorize("hasRole(T(com.jolly.astra.security.AuthoritiesConstants).ADMIN)")
	public MessageDto getRestricted() {
    log.info("restricted");
		return new MessageDto("You are an admin!");
	}

  @GetMapping("/secured")
  public MessageDto getSecured() {
    log.info("secured");
    return new MessageDto("You are an admin!");
  }

	public record MessageDto(String body) {
	}
}
