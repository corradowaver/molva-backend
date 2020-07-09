package com.molva.server.controller.management;

import com.molva.server.data.exceptions.file.FileExceptions;
import com.molva.server.data.exceptions.profile.ProfileExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import com.molva.server.data.service.ApplicationUserService;
import com.molva.server.data.service.ProfileService;
import com.molva.server.data.service.storage.AmazonClientService;
import com.molva.server.security.jwt.JwtProvider;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;

@RestController
@RequestMapping("management/api/v1/profile")
public class ProfileController {

  private final ProfileService profileService;
  private final ApplicationUserService applicationUserService;
  private final AmazonClientService amazonClientService;
  private final JwtProvider jwtProvider;

  @Autowired
  public ProfileController(ProfileService profileService,
                           ApplicationUserService applicationUserService,
                           AmazonClientService amazonClientService,
                           JwtProvider jwtProvider) {
    this.profileService = profileService;
    this.applicationUserService = applicationUserService;
    this.amazonClientService = amazonClientService;
    this.jwtProvider = jwtProvider;
  }

  @PutMapping(path = "/edit/photo/{profileId}")
  @PreAuthorize("hasAuthority('moderator:write')")
  public Profile updateProfilePhoto(@NotNull @PathVariable("profileId") Long profileId,
                                    @RequestHeader("Authorization") String token,
                                    @RequestPart String description,
                                    @RequestPart("file") MultipartFile file) {
    try {
      checkIfProfileIdMatches(token, profileId);
      URL url = amazonClientService.uploadNewProfilePhotoFile(file);
      Profile profile = profileService.loadProfileById(profileId);
      profile.setPhoto(url.toString());
      profileService.updateProfileById(profileId, profile);
      return profileService.updateProfileById(profileId, profile);
    } catch (IOException ex) {
      throw new FileExceptions.InvalidFileException();
    }
  }

  @PutMapping(path = "/edit/data/{profileId}")
  @PreAuthorize("hasAuthority('moderator:write')")
  public Profile updateProfileData(@NotNull @PathVariable("profileId") Long profileId,
                                   @RequestHeader("Authorization") String token,
                                   @RequestBody Profile profile) {
    checkIfProfileIdMatches(token, profileId);
    profileService.updateProfileById(profileId, profile);
    return profileService.updateProfileById(profileId, profile);
  }

  private void checkIfProfileIdMatches(String token, Long id) {
    ApplicationUser applicationUser = applicationUserService.loadAccountByUsername(
        jwtProvider.getUsername(jwtProvider.resolveToken(token))
    );
    if (!applicationUser.getProfile().getId().equals(id)) {
      throw new ProfileExceptions.ProfileNotFoundException();
    }
  }
}
