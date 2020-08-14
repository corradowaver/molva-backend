package com.molva.server.data.service.helpers;

import com.molva.server.data.exceptions.file.FileExceptions;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public class FileValidators {

  public static void validateMediaFile(MultipartFile file) {
    if (file == null) return;
    Optional<String> extension = getExtensionByFilename(file.getOriginalFilename());
    if (extension.isPresent()) {
      if (extension.get().equals("jpg") || extension.get().equals("png")) {
        validateImage(file);
      } else if (extension.get().equals("mp4")) {
        validateVideo(file);
      }
      return;
    }
    throw new FileExceptions.InvalidExtensionException();
  }

  public static void validateImage(MultipartFile file) {
    Optional<String> extension = getExtensionByFilename(file.getOriginalFilename());
    if (extension.isEmpty() || !extension.get().equals("jpg") && !extension.get().equals("png")) {
      throw new FileExceptions.InvalidExtensionException();
    }
    if (getFileSizeMegaBytes(file) > 10) {
      throw new FileExceptions.TooLargeFileException();
    }
  }

  public static void validateVideo(MultipartFile file) {
    if (getFileSizeMegaBytes(file) > 2000) {
      throw new FileExceptions.TooLargeFileException();
    }
  }

  public static long getFileSizeMegaBytes(MultipartFile file) {
    return file.getSize() / (1024 * 1024);
  }

  public static Optional<String> getExtensionByFilename(String filename) {
    return Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1));
  }
}
