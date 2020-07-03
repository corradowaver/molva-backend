package com.molva.server.data.service;

import com.molva.server.data.exceptions.user.UserExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.repository.ApplicationUserRepository;
import com.molva.server.security.roles.ApplicationUserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.security.InvalidParameterException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ApplicationUserService implements UserDetailsService {

  private final ApplicationUserRepository applicationUserRepository;

  private final PasswordEncoder passwordEncoder;

  public ApplicationUserService(ApplicationUserRepository applicationUserRepository, PasswordEncoder passwordEncoder) {
    this.applicationUserRepository = applicationUserRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public void registerUser(ApplicationUser applicationUser, ApplicationUserRole role) throws InvalidParameterException {
    if (isDataValid(applicationUser.getUsername(), applicationUser.getPassword())) {
      Optional<UserDetails> user = applicationUserRepository.findUserByUsername(applicationUser.getUsername());
      if (user.isPresent()) {
        throw new UserExceptions.UserAlreadyExistsException();
      }
      applicationUser.setApplicationUserRole(role);
      applicationUser.setPassword(passwordEncoder.encode(applicationUser.getPassword()));
      applicationUser.setAccountNonExpired(true);
      applicationUser.setAccountNonLocked(true);
      applicationUser.setCredentialsNotExpired(true);
      applicationUser.setEnabled(true);
      applicationUserRepository.save(applicationUser);
    } else {
      throw new UserExceptions.UserDataIsInvalidException();
    }
  }

  public boolean isDataValid(String username, String password) {
    Pattern usernamePattern = Pattern.compile("^[a-z0-9_-]{3,16}$");
    Pattern passwordPattern = Pattern.compile("^[a-zA-Z0-9_-]{6,18}$");
    Matcher u = usernamePattern.matcher(username);
    Matcher p = passwordPattern.matcher(password);
    return u.find() && p.find();
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return applicationUserRepository
        .findUserByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException(String.format("Username %s not found", username)));
  }

  public UserDetails loadUserById(Long id) {
    return applicationUserRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %s not found", id)));
  }
}
