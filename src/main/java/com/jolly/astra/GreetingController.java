package com.jolly.astra;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

	@GetMapping("/greet")
	public MessageDto getGreeting(Authentication auth) {
		return new MessageDto("Hi %s! You are granted with: %s.".formatted(auth.getName(), auth.getAuthorities()));
	}

	@GetMapping("/restricted")
	@PreAuthorize("hasAnyAuthority('ADMIN')")
	public MessageDto getRestricted() {
		return new MessageDto("You are an admin!");
	}

	public record MessageDto(String body) {
	}
}
