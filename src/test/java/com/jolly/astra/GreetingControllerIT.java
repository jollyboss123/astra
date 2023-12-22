package com.jolly.astra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author jolly
 */
@IntegrationTest
@AutoConfigureMockMvc
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

  @Test
  @WithMockAuthentication("ROLE_ADMIN")
  void userAuthorized_withAnnotation() throws Exception {
    api.perform(get("/restricted"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.body").value("You are an admin!"));
  }

  @Test
  @WithMockAuthentication("ROLE_USER")
  void userForbidden_withAnnotation() throws Exception {
    api.perform(get("/restricted"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithAnonymousUser
  void userNotAuthorized_withAnnotation() throws Exception {
    api.perform(get("/restricted"))
      .andExpect(status().isUnauthorized());
  }
}
