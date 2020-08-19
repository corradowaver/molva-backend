package com.molva.server.data.service.helpers;

import com.molva.server.data.exceptions.project.ProjectExceptions;

public class ProfileValidators {

  public static void validateFirstName(String firstName) {
    if (firstName.length() >= 20 || firstName.length() < 2) {
      throw new ProjectExceptions.ProjectInvalidNameException();
    }
  }

  public static void validateLastName(String lastName) {
    if (lastName.length() >= 20 || lastName.length() < 2) {
      throw new ProjectExceptions.ProjectInvalidNameException();
    }
  }
}
