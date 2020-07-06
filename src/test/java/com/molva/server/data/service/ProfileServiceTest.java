package com.molva.server.data.service;

import com.molva.server.data.exceptions.profile.ProfileExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import com.molva.server.data.repository.ProfileRepository;
import com.molva.server.helpers.ApplicationUserFactory;
import com.molva.server.helpers.ProfileFactory;
import com.molva.server.helpers.ProjectFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ProfileServiceTest {

  @Autowired
  ProfileService service;

  @MockBean
  private ProfileRepository repository;

  @Autowired
  ProjectFactory projectFactory;

  @Autowired
  ProfileFactory profileFactory;

  @Autowired
  ApplicationUserFactory applicationUserFactory;

  @Test
  void loadAllProfiles() {
    List<Profile> profiles = profileFactory.createProfilesList();
    doReturn(profiles).when(repository).findAll();
    List<Profile> returnedProfiles = service.loadAllProfiles();
    assertEquals(returnedProfiles, profiles);
  }

  @Test
  void loadProfileById() {
    Profile profile = profileFactory.createProfile();
    doReturn(Optional.of(profile)).when(repository).findById(any(Long.class));
    Profile returnedProfile = service.loadProfileById(any(Long.class));
    assertEquals(returnedProfile, profile);
  }

  @Test
  void loadProfileByApplicationUser() {
    ApplicationUser user = applicationUserFactory.createApplicationUser();
    Profile profile = profileFactory.createProfile();
    doReturn(Optional.of(profile)).when(repository).findProfileByApplicationUser(any(ApplicationUser.class));
    Profile returnedProfile = service.loadProfileByApplicationUser(user);
    assertEquals(returnedProfile, profile);
  }

  @Test
  void addProfile() {
    ApplicationUser user = applicationUserFactory.createApplicationUser();
    Profile profile = profileFactory.createProfile();
    doReturn(profile).when(repository).save(any(Profile.class));
    service.addProfile(profile, user);
    verify(repository, times(1)).save(profile);
  }

  @Test
  void updateProfileById() {
    Profile profile = profileFactory.createProfile();
    doReturn(Optional.of(profile)).when(repository).findById(any(Long.class));
    doReturn(profile).when(repository).save(any(Profile.class));
    service.updateProfileById(any(Long.class), profile);
    verify(repository, times(1)).save(any(Profile.class));
  }

  @Test
  void deleteProfileById() {
    Profile profile = profileFactory.createProfile();
    doReturn(Optional.of(profile)).when(repository).findById(any(Long.class));
    doNothing().when(repository).deleteById(any(Long.class));
    service.deleteProfileById(any(Long.class));
    verify(repository, times(1)).deleteById(any(Long.class));
  }

  @Test
  void addProfileMustFailIfProfileExists() {
    ApplicationUser existingUser = applicationUserFactory.createApplicationUser();
    Profile profile = profileFactory.createProfile();
    doReturn(Optional.of(existingUser)).when(repository).findProfileByApplicationUser(any(ApplicationUser.class));
    assertThrows(ProfileExceptions.ProfileExistsException.class,
        () -> service.addProfile(profile, existingUser));
  }

  @Test
  void loadProfileByIdMustFailIfNotFound() {
    doReturn(Optional.empty()).when(repository).findById(any(Long.class));
    assertThrows(ProfileExceptions.ProfileNotFoundException.class,
        () -> service.loadProfileById(any(Long.class)));
  }

  @Test
  void loadAllProfileByProfileMustFailIfNotFound() {
    doReturn(Optional.empty()).when(repository).findProfileByApplicationUser(any(ApplicationUser.class));
    assertThrows(ProfileExceptions.ProfileNotFoundException.class,
        () -> service.loadProfileByApplicationUser(any(ApplicationUser.class)));
  }

  @Test
  void updateProfileByIdMustFailIfNotFound() {
    Profile profile = profileFactory.createProfile();
    doReturn(Optional.empty()).when(repository).findById(any(Long.class));
    assertThrows(ProfileExceptions.ProfileNotFoundException.class,
        () -> service.updateProfileById(any(Long.class), profile));
  }

  @Test
  void deleteProfileByIdMustFailIfNotFound() {
    doReturn(Optional.empty()).when(repository).findById(any(Long.class));
    assertThrows(ProfileExceptions.ProfileNotFoundException.class,
        () -> service.deleteProfileById(any(Long.class)));
  }

}
