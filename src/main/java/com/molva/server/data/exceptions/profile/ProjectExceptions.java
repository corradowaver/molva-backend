package com.molva.server.data.exceptions.profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ProjectExceptions {
  @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Project was not found")
  public static class ProjectNotFoundException extends RuntimeException {
  }

  @ResponseStatus(code = HttpStatus.CONFLICT, reason = "Project already exists")
  public static class ProjectExistsException extends RuntimeException {
  }
}
