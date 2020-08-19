package com.molva.server.data.service;

import com.molva.server.data.exceptions.profile.ProfileExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.MediaFile;
import com.molva.server.data.model.Profile;
import com.molva.server.data.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {
  private final ProfileRepository profileRepository;
  private final MediaFileService mediaFileService;

  @Autowired
  ProfileService(ProfileRepository profileRepository, MediaFileService mediaFileService) {
    this.profileRepository = profileRepository;
    this.mediaFileService = mediaFileService;
  }

  public List<Profile> loadAllProfiles() {
    return (List<Profile>) profileRepository.findAll();
  }

  public Profile loadProfileById(Long id) {
    return profileRepository
        .findById(id)
        .orElseThrow(ProfileExceptions.ProfileNotFoundException::new);
  }

  public Profile loadProfileByApplicationUser(ApplicationUser applicationUser) {
    return profileRepository
        .findProfileByApplicationUser(applicationUser)
        .orElseThrow(ProfileExceptions.ProfileNotFoundException::new);
  }

  public Profile addProfile(Profile profile, ApplicationUser applicationUser) {
    Optional<Profile> profileWithProvidedApplicationUser =
        profileRepository.findProfileByApplicationUser(applicationUser);
    if (profileWithProvidedApplicationUser.isPresent()) {
      throw new ProfileExceptions.ProfileExistsException();
    }
    profile.setFirstname("");
    profile.setLastname("");
    return profileRepository.save(profile);
  }

  public Profile updateProfileById(Long id, Profile newProfile, MultipartFile newPhoto) {
    Optional<Profile> profileOptional = profileRepository.findById(id);
    if (profileOptional.isPresent()) {
      Profile oldProfile = profileOptional.get();
      MediaFile oldPhoto = oldProfile.getPhoto();
      oldProfile.setFirstname(newProfile.getFirstname());
      oldProfile.setLastname(newProfile.getLastname());
      if (newPhoto != null) {
        if (oldPhoto != null) {
          mediaFileService.deleteMediaFileById(oldPhoto.getId());
        }
        MediaFile savedPhoto = mediaFileService.addMediaFile(newPhoto);
        oldProfile.setPhoto(savedPhoto);
        savedPhoto.setPhotoOwner(oldProfile);
        mediaFileService.updateMediaFileById(savedPhoto.getId(), savedPhoto);
      }
      return profileRepository.save(oldProfile);
    } else {
      throw new ProfileExceptions.ProfileNotFoundException();
    }
  }

  public void deleteProfileById(Long id) {
    Optional<Profile> profileOptional = profileRepository.findById(id);
    if (profileOptional.isPresent()) {
      profileRepository.deleteById(id);
    } else {
      throw new ProfileExceptions.ProfileNotFoundException();
    }
  }
}
