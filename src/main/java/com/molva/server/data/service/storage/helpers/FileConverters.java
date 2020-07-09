package com.molva.server.data.service.storage.helpers;

import com.molva.server.data.exceptions.file.FileExceptions;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class FileConverters {

  public static File convertMultiPartToImageFile(MultipartFile file) throws IOException {
    File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
    FileOutputStream fos = new FileOutputStream(convFile);
    fos.write(file.getBytes());
    fos.close();
    return validateProfilePhotoFile(convFile);
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

  private static long getFileSizeMegaBytes(File file) {
    return file.length() / (1024 * 1024);
  }

  private static long getFileSizeKiloBytes(File file) {
    return file.length() / 1024;
  }

  private static long getFileSizeBytes(File file) {
    return file.length();
  }

  public static Optional<String> getExtensionByStringHandling(String filename) {
    return Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1));
  }
}
