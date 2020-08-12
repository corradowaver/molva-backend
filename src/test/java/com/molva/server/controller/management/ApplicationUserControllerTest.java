package com.molva.server.controller.management;

import com.molva.server.data.exceptions.user.UserExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.service.ApplicationUserService;
import com.molva.server.data.service.ProfileService;
import com.molva.server.helpers.ApplicationUserFactory;
import com.molva.server.security.jwt.JwtProvider;
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

import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationUserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ApplicationUserFactory factory;

  @MockBean
  ApplicationUserService userService;

  @MockBean
  ProfileService profileService;

  @MockBean
  JwtProvider jwtProvider;

  @Test
  @WithMockUser(authorities = "moderator:write")
    //TODO Почему модератор может, а админ не может -_-
  void deleteApplicationUserById() throws Exception {
    doNothing().when(userService).deleteApplicationUserById(any(Long.class));
    mockMvc.perform(delete("/management/api/v1/account/delete/{applicationUserId}", "4")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserUsernameMustReturnOkIfValidData() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    final String OLD_USERNAME = "oldUsername";
    final String NEW_USERNAME = "newUsername";

    doReturn("ResolvedToken").when(jwtProvider).resolveToken(any(String.class));
    doReturn(OLD_USERNAME).when(jwtProvider).getUsername(any(String.class));
    doReturn(user).when(userService).loadAccountByUsername(OLD_USERNAME);
    doReturn("ValidToken").when(jwtProvider).createToken(any(String.class), any(HashSet.class));
    doThrow(UserExceptions.UserNotFoundException.class).when(userService).loadAccountByUsername(NEW_USERNAME);
    doReturn(user).when(userService).loadUserById(any(Long.class));
    mockMvc.perform(put("/management/api/v1/account/edit/username/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(NEW_USERNAME)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserUsernameMustReturnBadRequestIfBadRequest() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    mockMvc.perform(put("/management/api/v1/account/edit/username/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserUsernameMustReturnConflictIfUsernameExists() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    final String OLD_USERNAME = "oldUsername";
    final String NEW_USERNAME = "newUsername";

    doReturn("ResolvedToken").when(jwtProvider).resolveToken(any(String.class));
    doReturn(OLD_USERNAME).when(jwtProvider).getUsername(any(String.class));
    doReturn(user).when(userService).loadAccountByUsername(OLD_USERNAME);
    doReturn("ValidToken").when(jwtProvider).createToken(any(String.class), any(HashSet.class));
    doReturn(user).when(userService).loadAccountByUsername(NEW_USERNAME);
    doReturn(user).when(userService).loadUserById(any(Long.class));
    mockMvc.perform(put("/management/api/v1/account/edit/username/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(NEW_USERNAME)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserUsernameMustReturnForbiddenIfInvalidToken() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    ApplicationUser fakeUser = factory.createRegisteredModerator(5L);
    final String OLD_USERNAME = "oldUsername";
    final String NEW_USERNAME = "newUsername";

    doReturn("ResolvedToken").when(jwtProvider).resolveToken(any(String.class));
    doReturn(OLD_USERNAME).when(jwtProvider).getUsername(any(String.class));
    doReturn(fakeUser).when(userService).loadAccountByUsername(OLD_USERNAME);
    doReturn("ValidToken").when(jwtProvider).createToken(any(String.class), any(HashSet.class));
    doReturn(user).when(userService).loadAccountByUsername(NEW_USERNAME);
    doReturn(user).when(userService).loadUserById(any(Long.class));
    mockMvc.perform(put("/management/api/v1/account/edit/username/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(NEW_USERNAME)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserEmailMustReturnOkIfValidData() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    final String NEW_EMAIL = "newEmail";

    doReturn("ResolvedToken").when(jwtProvider).resolveToken(any(String.class));
    doReturn(user.getUsername()).when(jwtProvider).getUsername(any(String.class));
    doReturn(user).when(userService).loadAccountByUsername(user.getUsername());
    doReturn("ValidToken").when(jwtProvider).createToken(any(String.class), any(HashSet.class));
    doThrow(UserExceptions.UserNotFoundException.class).when(userService).loadAccountByEmail(NEW_EMAIL);
    doReturn(user).when(userService).loadUserById(any(Long.class));
    mockMvc.perform(put("/management/api/v1/account/edit/email/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(NEW_EMAIL)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserEmailMustReturnMustReturnBadRequestIfBadRequest() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    mockMvc.perform(put("/management/api/v1/account/edit/email/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }


  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserEmailMustReturnConflictIfUsernameExist() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    final String NEW_EMAIL = "newEmail";

    doReturn("ResolvedToken").when(jwtProvider).resolveToken(any(String.class));
    doReturn(user.getUsername()).when(jwtProvider).getUsername(any(String.class));
    doReturn(user).when(userService).loadAccountByUsername(user.getUsername());
    doReturn("ValidToken").when(jwtProvider).createToken(any(String.class), any(HashSet.class));
    doReturn(user).when(userService).loadAccountByEmail(NEW_EMAIL);
    doReturn(user).when(userService).loadUserById(any(Long.class));
    mockMvc.perform(put("/management/api/v1/account/edit/email/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(NEW_EMAIL)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserEmailMustReturnForbiddenIfInvalidToken() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    ApplicationUser fakeUser = factory.createRegisteredModerator(5L);
    final String NEW_EMAIL = "newEmail";

    doReturn("ResolvedToken").when(jwtProvider).resolveToken(any(String.class));
    doReturn(user.getUsername()).when(jwtProvider).getUsername(any(String.class));
    doReturn(fakeUser).when(userService).loadAccountByUsername(user.getUsername());
    doReturn("ValidToken").when(jwtProvider).createToken(any(String.class), any(HashSet.class));
    doThrow(UserExceptions.UserNotFoundException.class).when(userService).loadAccountByEmail(NEW_EMAIL);
    doReturn(user).when(userService).loadUserById(any(Long.class));
    mockMvc.perform(put("/management/api/v1/account/edit/email/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(NEW_EMAIL)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserPasswordMustReturnOkIfValidData() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    final String NEW_PASSWORD = "newPassword";

    doReturn("ResolvedToken").when(jwtProvider).resolveToken(any(String.class));
    doReturn(user.getUsername()).when(jwtProvider).getUsername(any(String.class));
    doReturn(user).when(userService).loadAccountByUsername(user.getUsername());
    doReturn("ValidToken").when(jwtProvider).createToken(any(String.class), any(HashSet.class));
    doReturn(user).when(userService).loadUserById(user.getId());

    mockMvc.perform(put("/management/api/v1/account/edit/password/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(NEW_PASSWORD)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserPasswordMustReturnMustReturnBadRequestIfBadRequest() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    mockMvc.perform(put("/management/api/v1/account/edit/password/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "moderator:write")
  void updateApplicationUserPasswordMustReturnForbiddenIfInvalidToken() throws Exception {
    ApplicationUser user = factory.createRegisteredModerator(4L);
    ApplicationUser fakeUser = factory.createRegisteredModerator(5L);
    final String NEW_PASSWORD = "newPassword";

    doReturn("ResolvedToken").when(jwtProvider).resolveToken(any(String.class));
    doReturn(user.getUsername()).when(jwtProvider).getUsername(any(String.class));
    doReturn(fakeUser).when(userService).loadAccountByUsername(user.getUsername());
    doReturn("ValidToken").when(jwtProvider).createToken(any(String.class), any(HashSet.class));
    doReturn(user).when(userService).loadUserById(user.getId());

    mockMvc.perform(put("/management/api/v1/account/edit/password/{applicationUserId}", user.getId())
        .header("Authorization", "Bearer token")
        .contentType(MediaType.APPLICATION_JSON)
        .content(NEW_PASSWORD)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
