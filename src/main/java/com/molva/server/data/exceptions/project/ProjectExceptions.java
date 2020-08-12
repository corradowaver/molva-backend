package com.molva.server.data.exceptions.project;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ProjectExceptions {
  @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Project was not found")
  public static class ProjectNotFoundException extends RuntimeException {
  }

  @ResponseStatus(code = HttpStatus.CONFLICT, reason = "Project already exists")
  public static class ProjectExistsException extends RuntimeException {
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid project name")
  public static class ProjectInvalidNameException extends RuntimeException {
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Invalid project description")
  public static class ProjectInvalidDescriptionException extends RuntimeException {
  }
}
