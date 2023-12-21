package com.jolly.astra;

import com.jolly.astra.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author jolly
 */
@WebMvcTest(controllers = GreetingController.class, properties = { "server.ssl.enabled=false" })
@Import({ SecurityConfiguration.class })
class GreetingControllerIT {
  @Autowired
  private MockMvc api;

  @Test
  void userNotAuthorized() throws Exception {
    api.perform(get("/greet"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void userAuthenticated() throws Exception {
    api.perform(get("/greet")
      .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(
        new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN")
      ))
    ).andExpect(status().isOk())
      .andExpect(jsonPath("$.body").value("Hi user! You are granted with: [ROLE_USER, ROLE_ADMIN]."));
  }

  @Test
  void userAuthorized() throws Exception {
    api.perform(get("/restricted")
      .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(
        new SimpleGrantedAuthority("ROLE_ADMIN")
      ))
    ).andExpect(status().isOk())
      .andExpect(jsonPath("$.body").value("You are an admin!"));
  }

  @Test
  void userForbidden() throws Exception {
    api.perform(get("/restricted")
      .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(
        new SimpleGrantedAuthority("ROLE_USER")
      ))
    ).andExpect(status().isForbidden());
  }
}
