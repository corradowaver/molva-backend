package com.molva.server.data.service;

import com.molva.server.data.exceptions.profile.ProfileExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import com.molva.server.data.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {
  private final ProfileRepository profileRepository;

  @Autowired
  ProfileService(ProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
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
    return profileRepository.save(profile);
  }

  public Profile updateProfileById(Long id, Profile newProfile) {
    Optional<Profile> profileOptional = profileRepository.findById(id);
    if (profileOptional.isPresent()) {
      newProfile.setId(profileOptional.get().getId());
      return profileRepository.save(newProfile);
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
