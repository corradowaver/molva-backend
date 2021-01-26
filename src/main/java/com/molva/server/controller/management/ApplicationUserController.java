package com.molva.server.controller.management;

import com.molva.server.data.exceptions.user.UserExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.service.ApplicationUserService;
import com.molva.server.security.jwt.JwtProvider;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("management/api/v1/account")
public class ApplicationUserController {

  private final ApplicationUserService applicationUserService;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;
  private final String EMAIL_KEY = "EMAIL";
  private final String USERNAME_KEY = "USERNAME";

  @Autowired
  public ApplicationUserController(ApplicationUserService applicationUserService, JwtProvider jwtProvider, PasswordEncoder passwordEncoder) {
    this.applicationUserService = applicationUserService;
    this.jwtProvider = jwtProvider;
    this.passwordEncoder = passwordEncoder;
  }

  @DeleteMapping(path = "/delete/{applicationUserId}")
  @PreAuthorize("hasAuthority('moderator:write')")
  public void deleteApplicationUserById(@NotNull @PathVariable("applicationUserId") Long applicationUserId,
                                        @RequestHeader("Authorization") String token) {
    checkIfIdMatches(token, applicationUserId);
    applicationUserService.deleteApplicationUserById(applicationUserId);
  }

  @PutMapping(path = "/edit/username/{applicationUserId}")
  @PreAuthorize("hasAuthority('moderator:write')")
  public ApplicationUser updateApplicationUserUsername(@NotNull @PathVariable("applicationUserId") Long applicationUserId,
                                                       @RequestHeader("Authorization") String token,
                                                       @RequestParam("username") String newUsername,
                                                       HttpServletResponse response) {
    ApplicationUser applicationUser = checkIfIdMatches(token, applicationUserId);
    String newToken = jwtProvider.createToken(newUsername, applicationUser.getAuthorities());
    response.setHeader("Authorization", newToken);
    return updateDataByKey(USERNAME_KEY, applicationUserId, newUsername);
  }

  @PutMapping(path = "/edit/email/{applicationUserId}")
  @PreAuthorize("hasAuthority('moderator:write')")
  public ApplicationUser updateApplicationUserEmail(@NotNull @PathVariable("applicationUserId") Long applicationUserId,
                                                    @RequestHeader("Authorization") String token,
                                                    @RequestParam("email") String newEmail) {
    checkIfIdMatches(token, applicationUserId);
    return updateDataByKey(EMAIL_KEY, applicationUserId, newEmail);
  }

  @PutMapping(path = "/edit/password/{applicationUserId}")
  @PreAuthorize("hasAuthority('moderator:write')")
  public ApplicationUser updateApplicationUserPassword(@NotNull @PathVariable("applicationUserId") Long applicationUserId,
                                                       @RequestHeader("Authorization") String token,
                                                       @RequestParam("password") String newPassword) {
    checkIfIdMatches(token, applicationUserId);
    ApplicationUser updatedUser = applicationUserService.loadUserById(applicationUserId);
    updatedUser.setPassword(passwordEncoder.encode(newPassword));
    return applicationUserService.updateApplicationUserById(applicationUserId, updatedUser);
  }

  private ApplicationUser checkIfIdMatches(String token, Long id) {
    ApplicationUser applicationUser = applicationUserService.loadAccountByUsername(
        jwtProvider.getUsername(jwtProvider.resolveToken(token))
    );
    if (!applicationUser.getId().equals(id)) {
      throw new UserExceptions.UserNotFoundException();
    }
    return applicationUser;
  }

  private ApplicationUser updateDataByKey(String key, Long applicationUserId, String newData) {
    try {
      switch (key) {
        case EMAIL_KEY -> applicationUserService.loadAccountByEmail(newData);
        case USERNAME_KEY -> applicationUserService.loadAccountByUsername(newData);
      }
      throw new UserExceptions.UserAlreadyExistsException();
    } catch (UserExceptions.UserNotFoundException ex) {
      ApplicationUser updatedUser = applicationUserService.loadUserById(applicationUserId);
      switch (key) {
        case EMAIL_KEY -> updatedUser.setEmail(newData);
        case USERNAME_KEY -> updatedUser.setUsername(newData);
      }
      return applicationUserService.updateApplicationUserById(applicationUserId, updatedUser);
    }
  }
}
