package com.molva.server.data.service.storage.helpers;

import com.molva.server.data.exceptions.file.FileExceptions;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class FileConverters {

  public static final String PROFILE_PHOTO_KEY = "PROFILE_PHOTO";
  public static final String PROJECT_PREVIEW_KEY = "PROJECT_PREVIEW";

  public static File convertMultiPartToFile(MultipartFile file, String key) throws IOException {
    File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
    FileOutputStream fos = new FileOutputStream(convFile);
    fos.write(file.getBytes());
    fos.close();
    switch (key) {
      case PROFILE_PHOTO_KEY:
        return validateProfilePhotoFile(convFile);
      case PROJECT_PREVIEW_KEY:
        Optional<String> extension = getExtensionByStringHandling(file.getName());
        if (extension.isPresent()) {
          if (extension.get().equals("jpg") || extension.get().equals("png")) {
            return validateProjectPhotoFile(convFile);
          } else if (extension.get().equals("mp4")) {
            return validateProjectVideoFile(convFile);
          } else {
            throw new FileExceptions.InvalidExtensionException();
          }
        }
      default:
        throw new FileExceptions.InvalidFileException();
    }
  }

  private static File validateProfilePhotoFile(File file) {
    Optional<String> extension = getExtensionByStringHandling(file.getName());
    if (extension.isEmpty() || !extension.get().equals("jpg") && !extension.get().equals("png")) {
      file.delete();
      throw new FileExceptions.InvalidExtensionException();
    }
    if (getFileSizeMegaBytes(file) > 10) {
      file.delete();
      throw new FileExceptions.TooLargeFileException();
    }
    return file;
  }

  private static File validateProjectPhotoFile(File file) {
    if (getFileSizeMegaBytes(file) > 100) {
      file.delete();
      throw new FileExceptions.TooLargeFileException();
    }
    return file;
  }

  private static File validateProjectVideoFile(File file) {
    if (getFileSizeMegaBytes(file) > 2000) {
      file.delete();
      throw new FileExceptions.TooLargeFileException();
    }
    return file;
  }

  private static long getFileSizeMegaBytes(File file) {
    return file.length() / (1024 * 1024);
  }

  public static Optional<String> getExtensionByStringHandling(String filename) {
    return Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1));
  }
}
