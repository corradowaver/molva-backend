package com.molva.server.data.service.helpers;

import com.molva.server.data.exceptions.project.ProjectExceptions;

public class ProjectValidators {

  public static void validateProjectName(String name) {
    if (name.length() >= 66 || name.length() < 3) {
      throw new ProjectExceptions.ProjectInvalidNameException();
    }
  }

  public static void validateProjectDescription(String description) {
    if (description.split(" ").length > 2000 || description.length() > 15000) {
      throw new ProjectExceptions.ProjectInvalidDescriptionException();
    }
  }
}
