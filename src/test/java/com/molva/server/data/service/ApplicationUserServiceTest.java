package com.molva.server.data.service;

import com.molva.server.data.exceptions.user.UserExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.repository.ApplicationUserRepository;
import com.molva.server.helpers.ApplicationUserFactory;
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

  @Autowired
  ApplicationUserFactory applicationUserFactory;

  @MockBean
  private ApplicationUserRepository repository;

  @ParameterizedTest
  @MethodSource("validDataSource")
  void registerUserMustReturnCorrectUserIfValidData(String username, String password, String email) {
    ApplicationUser newUser = new ApplicationUser(username, password, email);
    doReturn(newUser).when(repository).save(any(ApplicationUser.class));
    service.registerUser(newUser, ApplicationUserRole.MODERATOR);
    verify(repository, times(1)).save(newUser);
  }

  @ParameterizedTest
  @MethodSource("invalidDataSource")
  void registerUserMustFailIfInvalidData(String username, String password, String email) {
    ApplicationUser newUser = new ApplicationUser(username, password, email);
    doReturn(null).when(repository).save(any(ApplicationUser.class));
    assertThrows(UserExceptions.UserDataIsInvalidException.class,
        () -> service.registerUser(newUser, ApplicationUserRole.MODERATOR));
  }

  @Test
  void registerUserMustFailIfUsernameAlreadyExists() {
    ApplicationUser existingUser = applicationUserFactory.createApplicationUser();
    doReturn(Optional.of(existingUser)).when(repository).findAccountByUsername(existingUser.getUsername());
    assertThrows(UserExceptions.UserAlreadyExistsException.class,
        () -> service.registerUser(existingUser, ApplicationUserRole.MODERATOR));
  }

  @Test
  void registerUserMustFailIfEmailAlreadyExists() {
    ApplicationUser existingUser = applicationUserFactory.createApplicationUser();
    doReturn(Optional.of(existingUser)).when(repository).findAccountByEmail(existingUser.getEmail());
    assertThrows(UserExceptions.UserAlreadyExistsException.class,
        () -> service.registerUser(existingUser, ApplicationUserRole.MODERATOR));
  }

  @ParameterizedTest
  @MethodSource("validDataSource")
  void isDataValidMustReturnTrueWithValidData(String username, String password, String email) {
    boolean isValid = service.isDataValid(username, password, email);
    assertTrue(isValid);
  }

  @ParameterizedTest
  @MethodSource("invalidDataSource")
  void isDataValidMustReturnFalseWithInvalidData(String username, String password, String email) {
    boolean isValid = service.isDataValid(username, password, email);
    assertFalse(isValid);
  }

  @Test
  void loadUserByUsername() {
    UserDetails user = applicationUserFactory.createApplicationUser();
    doReturn(Optional.of(user)).when(repository).findUserByUsername(user.getUsername());
    UserDetails returnedUser = service.loadUserByUsername(user.getUsername());
    assertEquals(returnedUser, user);
  }

  @Test
  void loadUserById() {
    UserDetails user = applicationUserFactory.createApplicationUser();
    doReturn(Optional.of(user)).when(repository).findById(1L);
    UserDetails returnedUser = service.loadUserById(1L);
    assertEquals(returnedUser, user);
  }

  private static Stream<Arguments> validDataSource() {
    return Stream.of(
        Arguments.of("username", "password", "valid@email.com"),
        Arguments.of("username1", "ThePassword1", "valid@email.com"),
        Arguments.of("us_", "pwd-_-", "valid@email.com")
    );
  }

  private static Stream<Arguments> invalidDataSource() {
    return Stream.of(
        Arguments.of("us", "pwd", "a1"),
        Arguments.of("username@", "password!", "email"),
        Arguments.of("superLongUsername", "SuperSuperLongPassword", "invalid@mail")
    );
  }
}
