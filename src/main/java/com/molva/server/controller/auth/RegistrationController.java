package com.molva.server.controller.auth;

import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import com.molva.server.data.service.ApplicationUserService;
import com.molva.server.data.service.ProfileService;
import com.molva.server.data.service.ProjectService;
import com.molva.server.security.roles.ApplicationUserRole;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/api/v1/registration")
public class RegistrationController {

  private final ApplicationUserService applicationUserService;
  private final ProfileService profileService;

  @Autowired
  public RegistrationController(ApplicationUserService applicationUserService, ProfileService profileService, ProjectService projectService) {
    this.applicationUserService = applicationUserService;
    this.profileService = profileService;
  }


  @PostMapping(path = "/register/moderator")
  public ApplicationUser registerModerator(@NotNull @RequestBody ApplicationUser applicationUser) {
    ApplicationUser account = applicationUserService.registerUser(applicationUser, ApplicationUserRole.MODERATOR);
    Profile profile = new Profile();
    profile.setApplicationUser(account);
    profileService.addProfile(profile, account);
    account.setProfile(profile);
    applicationUserService.updateApplicationUserById(account.getId(), account);
    return account;
  }
}
