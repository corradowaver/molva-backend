package com.molva.server.data.service;

import com.molva.server.data.exceptions.user.UserExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.repository.ApplicationUserRepository;
import com.molva.server.security.roles.ApplicationUserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ApplicationUserServiceTest {

  @Autowired
  ApplicationUserService service;

  @MockBean
  private ApplicationUserRepository repository;

  @ParameterizedTest
  @MethodSource("validDataSource")
  void registerUserMustReturnCorrectUserIfValidData(String username, String password) {
    ApplicationUser newUser = new ApplicationUser(username, password);
    doReturn(newUser).when(repository).save(any(ApplicationUser.class));
    service.registerUser(newUser, ApplicationUserRole.MODERATOR);
    verify(repository, times(1)).save(newUser);
  }

  @ParameterizedTest
  @MethodSource("invalidDataSource")
  void registerUserMustFailIfInvalidData(String username, String password) {
    ApplicationUser newUser = new ApplicationUser(username, password);
    doReturn(null).when(repository).save(any(ApplicationUser.class));
    assertThrows(UserExceptions.UserDataIsInvalidException.class,
        () -> service.registerUser(newUser, ApplicationUserRole.MODERATOR));
  }

  @Test
  void registerUserMustFailIfUserAlreadyExists() {
    ApplicationUser existingUser = new ApplicationUser("username", "password");
    doReturn(Optional.of(existingUser)).when(repository).findUserByUsername(existingUser.getUsername());
    assertThrows(UserExceptions.UserAlreadyExistsException.class,
        () -> service.registerUser(existingUser, ApplicationUserRole.MODERATOR));
  }

  @ParameterizedTest
  @MethodSource("validDataSource")
  void isDataValidMustReturnTrueWithValidData(String username, String password) {
    boolean isValid = service.isDataValid(username, password);
    assertTrue(isValid);
  }

  @ParameterizedTest
  @MethodSource("invalidDataSource")
  void isDataValidMustReturnFalseWithInvalidData(String username, String password) {
    boolean isValid = service.isDataValid(username, password);
    assertFalse(isValid);
  }

  @Test
  void loadUserByUsername() {
    UserDetails user = new ApplicationUser("username1", "password1");
    doReturn(Optional.of(user)).when(repository).findUserByUsername(user.getUsername());
    UserDetails returnedUser = service.loadUserByUsername(user.getUsername());
    assertEquals(returnedUser, user);
  }

  @Test
  void loadUserById() {
    UserDetails user = new ApplicationUser("username1", "password1");
    doReturn(Optional.of(user)).when(repository).findById(1L);
    UserDetails returnedUser = service.loadUserById(1L);
    assertEquals(returnedUser, user);
  }

  private static Stream<Arguments> validDataSource() {
    return Stream.of(
        Arguments.of("username", "password"),
        Arguments.of("username1", "ThePassword1"),
        Arguments.of("us_", "pwd-_-")
    );
  }

  private static Stream<Arguments> invalidDataSource() {
    return Stream.of(
        Arguments.of("us", "pwd"),
        Arguments.of("username@", "password!"),
        Arguments.of("superLongUsername", "SuperSuperLongPassword")
    );
  }
}
