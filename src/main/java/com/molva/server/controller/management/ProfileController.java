package com.molva.server.controller.management;

import com.molva.server.data.exceptions.profile.ProfileExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import com.molva.server.data.service.ApplicationUserService;
import com.molva.server.data.service.ProfileService;
import com.molva.server.data.service.helpers.FileValidators;
import com.molva.server.data.service.helpers.ProfileValidators;
import com.molva.server.security.jwt.JwtProvider;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("management/api/v1/profile")
public class ProfileController {

  private final ProfileService profileService;
  private final ApplicationUserService applicationUserService;
  private final JwtProvider jwtProvider;

  @Autowired
  public ProfileController(ProfileService profileService,
                           ApplicationUserService applicationUserService,
                           JwtProvider jwtProvider) {
    this.profileService = profileService;
    this.applicationUserService = applicationUserService;
    this.jwtProvider = jwtProvider;
  }

  @PutMapping(path = "/edit/{profileId}")
  @PreAuthorize("hasAuthority('moderator:write')")
  public Profile updateProfileData(
      @NotNull @PathVariable("profileId") Long profileId,
      @RequestHeader("Authorization") String token,
      Profile profile,
      @RequestParam(value = "photo-multipart", required = false) MultipartFile photo) {

    throwAnExceptionIfProfileIdNotMatchesUserId(token, profileId);
    validateProfileData(profile, photo);
    return profileService.updateProfileById(profileId, profile, photo);
  }

  public void validateProfileData(
      Profile profileData,
      MultipartFile photo
  ) {
    ProfileValidators.validateFirstName(profileData.getFirstname());
    ProfileValidators.validateLastName(profileData.getLastname());
    FileValidators.validateMediaFile(photo);
  }

  private void throwAnExceptionIfProfileIdNotMatchesUserId(String token, Long id) {
    ApplicationUser applicationUser = applicationUserService.loadAccountByUsername(
        jwtProvider.getUsername(jwtProvider.resolveToken(token))
    );
    Profile profile = profileService.loadProfileByApplicationUser(applicationUser);
    if (!(profile.getApplicationUser().getId().equals(applicationUser.getId())) || !profile.getId().equals(id)) {
      throw new ProfileExceptions.ProfileNotFoundException();
    }
  }
}
