package com.molva.server.data.exceptions.file;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class FileExceptions {
  @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Extension is invalid")
  public static class InvalidExtensionException extends RuntimeException {
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "File is too large")
  public static class TooLargeFileException extends RuntimeException {
  }

  @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "File is invalid")
  public static class InvalidFileException extends RuntimeException {
  }

  @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "File was not found")
  public static class FileNotFoundException extends RuntimeException {
  }

  @ResponseStatus(code = HttpStatus.CONFLICT, reason = "File already exists")
  public static class FileExistsException extends RuntimeException {
  }
}
