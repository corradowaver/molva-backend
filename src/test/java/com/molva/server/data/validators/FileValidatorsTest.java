package com.molva.server.data.validators;

import com.molva.server.data.exceptions.file.FileExceptions;
import com.molva.server.data.service.helpers.FileValidators;
import com.molva.server.helpers.MediaFileFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FileValidatorsTest {

  @Autowired
  MediaFileFactory mediaFileFactory;

  @Test
  public void validateImageMustThrowInvalidExtensionExceptionIfExtensionIsInvalid() {
    MultipartFile multipartFile = mediaFileFactory.createInvalidMultipartFile();
    assertThrows(FileExceptions.InvalidExtensionException.class, () -> FileValidators.validateImage(multipartFile));
  }

  @Test
  public void validateImageMustThrowTooLargeFileExceptionIfFileIsTooLarge() {
    MultipartFile multipartFile = mediaFileFactory.createLargeMultipartFile();
    assertThrows(FileExceptions.TooLargeFileException.class, () -> FileValidators.validateImage(multipartFile));
  }

  @Test
  public void validateVideoMustThrowTooLargeFileExceptionIfFileIsTooLarge() {
    MultipartFile multipartFile = mediaFileFactory.createLargeMultipartFile();
    assertThrows(FileExceptions.TooLargeFileException.class, () -> FileValidators.validateVideo(multipartFile));
  }

  @Test
  public void getExtensionByFilenameMustReturnCorrectExtension() {
    Optional<String> ext = FileValidators.getExtensionByFilename("belle.jpg");
    if (ext.isPresent()) {
      assertEquals("jpg", ext.get());
    } else {
      fail("Extension must not be empty");
    }
  }

  @Test
  public void getExtensionByFilenameMustReturnEmptyOptionalIfNoExtensionIsPresent() {
    assertTrue(FileValidators.getExtensionByFilename("belle").isEmpty());
  }
}
