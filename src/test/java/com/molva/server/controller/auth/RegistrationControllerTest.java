package com.molva.server.controller.auth;

import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import com.molva.server.data.service.ApplicationUserService;
import com.molva.server.data.service.ProfileService;
import com.molva.server.helpers.ApplicationUserFactory;
import com.molva.server.security.roles.ApplicationUserRole;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ApplicationUserFactory factory;

  @MockBean
  ApplicationUserService userService;

  @MockBean
  ProfileService profileService;


  @Test
  @WithMockUser(roles = "ADMIN")
  void registrationModeratorMustReturnOkIfValidRequest() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    var jsonBody = new JSONObject(
        Map.of(
            "username", user.getUsername(),
            "password", user.getPassword(),
            "email", user.getEmail()));

    doReturn(user).when(userService).registerUser(any(ApplicationUser.class), any(ApplicationUserRole.class));
    doReturn(null).when(profileService).addProfile(any(Profile.class), any(ApplicationUser.class));
    doReturn(user).when(userService).updateApplicationUserById(any(Long.class), any(ApplicationUser.class));

    mockMvc.perform(post("/admin/api/v1/registration/register/moderator")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonBody.toString())
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.username").value(user.getUsername()))
        .andExpect(jsonPath("$.email").value(user.getEmail()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void registrationModeratorMustReturnBadRequestIfInvalidRequest() throws Exception {
    mockMvc.perform(post("/admin/api/v1/registration/register/moderator")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}
