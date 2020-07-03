package com.molva.server.data.exceptions.project;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ProfileExceptions {
  @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Profile was not found")
  public static class ProfileNotFoundException extends RuntimeException {
  }

  @ResponseStatus(code = HttpStatus.CONFLICT, reason = "Profile already exists")
  public static class ProfileExistsException extends RuntimeException {
  }
}
