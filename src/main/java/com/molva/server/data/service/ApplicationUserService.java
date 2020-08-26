package com.molva.server.data.service;

import com.google.common.collect.Sets;
import com.molva.server.data.exceptions.profile.ProfileExceptions;
import com.molva.server.data.exceptions.user.UserExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import com.molva.server.data.model.Project;
import com.molva.server.data.repository.ApplicationUserRepository;
import com.molva.server.security.roles.ApplicationUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ApplicationUserService implements UserDetailsService {

  private final ApplicationUserRepository applicationUserRepository;

  private final PasswordEncoder passwordEncoder;

  private final MediaFileService mediaFileService;

  private final ProjectService projectService;

  @Autowired
  public ApplicationUserService(ApplicationUserRepository applicationUserRepository, PasswordEncoder passwordEncoder,
                                MediaFileService mediaFileService, @Lazy ProjectService projectService) {
    this.applicationUserRepository = applicationUserRepository;
    this.passwordEncoder = passwordEncoder;
    this.mediaFileService = mediaFileService;
    this.projectService = projectService;
  }

  public ApplicationUser registerUser(ApplicationUser applicationUser, ApplicationUserRole role) throws InvalidParameterException {
    String username = applicationUser.getUsername();
    String password = applicationUser.getPassword();
    String email = applicationUser.getEmail();
    if (username != null && password != null && email != null && isDataValid(username, password, email)) {
      Optional<ApplicationUser> userByUsername = applicationUserRepository.findAccountByUsername(applicationUser.getUsername());
      Optional<ApplicationUser> userByEmail = applicationUserRepository.findAccountByEmail(applicationUser.getEmail());
      if (userByUsername.isPresent() || userByEmail.isPresent()) {
        throw new UserExceptions.UserAlreadyExistsException();
      }
      applicationUser.setApplicationUserRole(role);
      applicationUser.setCreatedProjects(Sets.newHashSet());
      applicationUser.setPassword(passwordEncoder.encode(applicationUser.getPassword()));
      applicationUser.setAccountNonExpired(true);
      applicationUser.setAccountNonLocked(true);
      applicationUser.setCredentialsNotExpired(true);
      applicationUser.setEnabled(true);
      applicationUserRepository.save(applicationUser);
      return applicationUser;
    } else {
      throw new UserExceptions.UserDataIsInvalidException();
    }
  }

  public boolean isDataValid(String username, String password, String email) {
    if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
      return false;
    }
    Pattern usernamePattern = Pattern.compile("^[a-z0-9_-]{3,16}$");
    Pattern passwordPattern = Pattern.compile("^[a-zA-Z0-9_-]{6,18}$");
    Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
    Matcher u = usernamePattern.matcher(username);
    Matcher p = passwordPattern.matcher(password);
    Matcher e = emailPattern.matcher(email);
    return u.find() && p.find() && e.find();
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return applicationUserRepository
        .findUserByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException(String.format("Username %s not found", username)));
  }

  public ApplicationUser loadAccountByUsername(String username) {
    return applicationUserRepository
        .findAccountByUsername(username)
        .orElseThrow(UserExceptions.UserNotFoundException::new);
  }

  public ApplicationUser loadAccountByEmail(String email) {
    return applicationUserRepository
        .findAccountByEmail(email)
        .orElseThrow(UserExceptions.UserNotFoundException::new);
  }

  public ApplicationUser loadUserById(Long id) {
    return applicationUserRepository
        .findById(id)
        .orElseThrow(UserExceptions.UserNotFoundException::new);
  }

  public ApplicationUser updateApplicationUserById(Long id, ApplicationUser newApplicationUser) {
    Optional<ApplicationUser> applicationUserOptional = applicationUserRepository.findById(id);
    if (applicationUserOptional.isPresent()) {
      newApplicationUser.setId(applicationUserOptional.get().getId());
      return applicationUserRepository.save(newApplicationUser);
    } else {
      throw new ProfileExceptions.ProfileNotFoundException();
    }
  }

  public void deleteApplicationUserById(Long id) {
    Optional<ApplicationUser> user = applicationUserRepository.findById(id);
    if (user.isPresent()) {
      Profile profile = user.get().getProfile();
      if ((profile != null) && (profile.getPhoto() != null)) {
        mediaFileService.deleteMediaFileById(profile.getPhoto().getId());
      }
      Set<Project> createdProjects = user.get().getCreatedProjects();
      if (!createdProjects.isEmpty()) {
        createdProjects.forEach(project -> projectService.deleteProjectById(project.getId()));
      }
    }
    applicationUserRepository.deleteById(id);
  }

}
