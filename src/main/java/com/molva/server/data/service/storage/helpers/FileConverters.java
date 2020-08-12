package com.molva.server.data.service.storage.helpers;

import com.molva.server.data.exceptions.file.FileExceptions;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class FileConverters {
  public static File convertMultiPartToFile(MultipartFile file) {
    File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
    try {
      FileOutputStream fos = new FileOutputStream(convFile);
      fos.write(file.getBytes());
      fos.close();
    } catch (IOException ex) {
      throw new FileExceptions.InvalidFileException();
    }
    return convFile;
  }
}
