package com.molva.server.data.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.molva.server.data.exceptions.file.FileExceptions;
import com.molva.server.data.model.MediaFile;
import com.molva.server.data.repository.MediaFileRepository;
import com.molva.server.data.service.helpers.FileConverters;
import com.molva.server.data.service.helpers.FileValidators;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class MediaFileService {
  private final MediaFileRepository mediaFileRepository;

  @Value("${gcs.bucket.name}")
  private String bucketName;

  Storage storage;

  static final String FILE_PATTERN = "file:";

  @Autowired
  MediaFileService(
      MediaFileRepository mediaFileRepository,
      @Value("${spring.cloud.gcp.credentials.location}") String rawCredentialsPath
  ) throws IOException {
    this.mediaFileRepository = mediaFileRepository;
    String credentialsPath = rawCredentialsPath.replaceFirst(Pattern.quote(FILE_PATTERN), "");
    Credentials credentials = GoogleCredentials.fromStream(
        new FileInputStream(credentialsPath)
    );
    storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
  }

  public MediaFile loadMediaFileById(Long id) {
    return mediaFileRepository
        .findById(id)
        .orElseThrow(FileExceptions.FileNotFoundException::new);
  }

  public byte[] loadMediaFileBytesById(Long id) {
    return getMediaFileBytes(loadMediaFileById(id).getPath());
  }

  public MediaFile addMediaFile(MultipartFile multipartFile) {
    if (multipartFile == null) return null;
    File convertedFile = FileConverters.convertMultiPartToFile(multipartFile);
    MediaFile savedFile = saveMediaFile(convertedFile);
    try {
      saveFileToStorage(convertedFile, savedFile.getPath());
      return savedFile;
    } catch (IOException ex) {
      deleteMediaFileById(savedFile.getId());
      throw new FileExceptions.InvalidFileException();
    }
  }

  public MediaFile saveMediaFile(File convertedFile) {
    try (InputStream is = new FileInputStream(convertedFile)) {
      BasicFileAttributes basicFileAttributes = Files.readAttributes(
          Paths.get(convertedFile.getAbsolutePath()), BasicFileAttributes.class
      );
      FileNameMap fileNameMap = URLConnection.getFileNameMap();
      String mimeType = fileNameMap.getContentTypeFor(convertedFile.getName());
      MediaFile mediaFile = new MediaFile(
          new Date(basicFileAttributes.creationTime().toMillis()),
          new Date(basicFileAttributes.lastModifiedTime().toMillis()),
          DigestUtils.md5Hex(is),
          mimeType,
          basicFileAttributes.size()
      );
      mediaFile = mediaFileRepository.save(mediaFile);
      String filename = getFilePath(mediaFile.getId()) + "."
          + FileValidators.getExtensionByFilename
          (
              convertedFile.getName()
          ).orElseThrow(FileExceptions.InvalidFileException::new);
      mediaFile.setPath(filename);
      return mediaFileRepository.save(mediaFile);
    } catch (IOException e) {
      convertedFile.delete();
      throw new FileExceptions.InvalidFileException();
    }
  }

  public void saveFileToStorage(File convertedFile, String filename) throws IOException {
    storage.create(
        BlobInfo
            .newBuilder(bucketName, filename)
            .build(),
        Files.readAllBytes(convertedFile.toPath())
    );
  }

  public void deleteMediaFileById(Long id) {
    String path = loadMediaFileById(id).getPath();
    storage.delete(BlobId.of(bucketName, path));
    mediaFileRepository.deleteById(id);
  }

  public MediaFile updateMediaFileById(Long id, MediaFile projectFile) {
    Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(id);
    if (mediaFileOptional.isPresent()) {
      projectFile.setId(mediaFileOptional.get().getId());
      return mediaFileRepository.save(projectFile);
    } else {
      throw new FileExceptions.FileNotFoundException();
    }
  }

  public byte[] getMediaFileBytes(String path) {
    Blob blob = storage.get(bucketName, path);
    return blob.getContent();
  }

  private String getFilePath(long projectId) throws IOException {
    StringBuilder filePathBuilder = new StringBuilder();
    byte[] bytes = longToByteArray(projectId);
    filePathBuilder
        .append(bytes[0])
        .append(bytes[1])
        .append("/")
        .append(bytes[2])
        .append(bytes[3])
        .append("/");
    for (int i = 4; i < bytes.length; i++) {
      filePathBuilder.append(bytes[i]);
    }
    return filePathBuilder.toString();
  }

  private byte[] longToByteArray(long i) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(bos);
    dos.writeLong(i);
    dos.flush();
    return bos.toByteArray();
  }
}
