package com.molva.server.data.service;

import com.molva.server.data.exceptions.file.FileExceptions;
import com.molva.server.data.model.MediaFile;
import com.molva.server.data.repository.MediaFileRepository;
import com.molva.server.helpers.MediaFileFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MediaFileServiceTest {

  @Autowired
  MediaFileService service;

  @MockBean
  private MediaFileRepository repository;

  @Autowired
  MediaFileFactory mediaFileFactory;

  @Test
  public void loadMediaFileById() {
    MediaFile mediaFile = mediaFileFactory.createMediaFile();
    doReturn(Optional.of(mediaFile)).when(repository).findById(any(Long.class));
    MediaFile returnedMediaFile = service.loadMediaFileById(any(Long.class));
    assertEquals(returnedMediaFile, service.loadMediaFileById(any(Long.class)));
  }

  @Test
  public void addMediaFile() {
    MultipartFile multipartFile = mediaFileFactory.createMultipartFile();
    MediaFile mediaFile = mediaFileFactory.createMediaFile();
    doReturn(mediaFile).when(repository).save(any(MediaFile.class));
    service.addMediaFile(multipartFile);
    verify(repository, times(1)).save(mediaFile);
  }

  @Test
  void updateMediaFileById() {
    MediaFile mediaFile = mediaFileFactory.createMediaFile();
    doReturn(Optional.of(mediaFile)).when(repository).findById(any(Long.class));
    doReturn(mediaFile).when(repository).save(any(MediaFile.class));
    service.updateMediaFileById(any(Long.class), mediaFile);
    verify(repository, times(1)).save(any(MediaFile.class));
  }

  @Test
  public void deleteMediaFileById() {
    MediaFile mediaFile = mediaFileFactory.createMediaFile();
    mediaFile.setPath("/00/ff/12A321AF222belle.jpg");
    doReturn(Optional.of(mediaFile)).when(repository).findById(any(Long.class));
    doNothing().when(repository).deleteById(any(Long.class));
    service.deleteMediaFileById(any(Long.class));
    verify(repository, times(1)).deleteById(any(Long.class));
  }

  @Test
  void loadMediaFileByIdMustFailIfNotFound() {
    doReturn(Optional.empty()).when(repository).findById(any(Long.class));
    assertThrows(FileExceptions.FileNotFoundException.class,
        () -> service.loadMediaFileById(any(Long.class)));
  }

  @Test
  void updateMediaFileByIdMustFailIfNotFound() {
    MediaFile MediaFile = mediaFileFactory.createMediaFile();
    doReturn(Optional.empty()).when(repository).findById(any(Long.class));
    assertThrows(FileExceptions.FileNotFoundException.class,
        () -> service.updateMediaFileById(any(Long.class), MediaFile));
  }

  @Test
  void deleteMediaFileByIdMustFailIfNotFound() {
    doReturn(Optional.empty()).when(repository).findById(any(Long.class));
    assertThrows(FileExceptions.FileNotFoundException.class,
        () -> service.deleteMediaFileById(any(Long.class)));
  }
}
