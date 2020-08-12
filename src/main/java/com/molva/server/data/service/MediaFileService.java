package com.molva.server.data.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.molva.server.data.exceptions.file.FileExceptions;
import com.molva.server.data.model.MediaFile;
import com.molva.server.data.repository.MediaFileRepository;
import com.molva.server.data.service.storage.helpers.FileConverters;
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

@Service
public class MediaFileService {
  private final MediaFileRepository mediaFileRepository;
  @Value("${gcs.bucket.name}")
  private String bucketName;
  String PATH_TO_JSON_KEY = "/usr/local/molva-285811-e8c348a24e2c.json";
  Storage storage;

  @Autowired
  MediaFileService(MediaFileRepository mediaFileRepository) throws IOException {
    this.mediaFileRepository = mediaFileRepository;
    Credentials credentials = GoogleCredentials.fromStream(
        new FileInputStream(PATH_TO_JSON_KEY)
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
    File file = FileConverters.convertMultiPartToFile(multipartFile);
    try (InputStream is = new FileInputStream(file)) {
      BasicFileAttributes basicFileAttributes = Files.readAttributes(
          Paths.get(file.getAbsolutePath()), BasicFileAttributes.class
      );
      FileNameMap fileNameMap = URLConnection.getFileNameMap();
      String mimeType = fileNameMap.getContentTypeFor(file.getName());
      MediaFile mediaFile = new MediaFile();
      mediaFile.setCreated(new Date(basicFileAttributes.creationTime().toMillis()));
      mediaFile.setUpdated(new Date(basicFileAttributes.lastModifiedTime().toMillis()));
      mediaFile.setMd5(DigestUtils.md5Hex(is));
      mediaFile.setSize(basicFileAttributes.size());
      mediaFile.setMime(mimeType);
      mediaFile = mediaFileRepository.save(mediaFile);
      String resultFilename = getFilePath(mediaFile.getId()) + "."
          + getExtensionByFilename(file.getName()).orElseThrow(FileExceptions.InvalidFileException::new);
      mediaFile.setPath(resultFilename);
      storage.create(
          BlobInfo
              .newBuilder(bucketName, resultFilename)
              .build(),
          Files.readAllBytes(file.toPath())
      );
      file.delete();
      return mediaFileRepository.save(mediaFile);
    } catch (IOException e) {
      file.delete();
      throw new FileExceptions.InvalidFileException();
    }
  }

  public void deleteMediaFileById(Long id) {
    MediaFile mediaFile = loadMediaFileById(id);
    storage.delete(BlobId.of(bucketName, mediaFile.getPath()));
    mediaFileRepository.deleteById(id);
  }

  public MediaFile updateMediaFileById(Long id, MediaFile mediaFile) {
    Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(id);
    if (mediaFileOptional.isPresent()) {
      mediaFile.setId(mediaFileOptional.get().getId());
      return mediaFileRepository.save(mediaFile);
    } else {
      throw new FileExceptions.FileNotFoundException();
    }
  }

  public byte[] getMediaFileBytes(String path) {
    Blob blob = storage.get(bucketName, path);
    return blob.getContent();
  }

  public void validateMediaFile(MultipartFile file) {
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

  public void validateImage(MultipartFile file) {
    Optional<String> extension = getExtensionByFilename(file.getOriginalFilename());
    if (extension.isEmpty() || !extension.get().equals("jpg") && !extension.get().equals("png")) {
      throw new FileExceptions.InvalidExtensionException();
    }
    if (getFileSizeMegaBytes(file) > 10) {
      throw new FileExceptions.TooLargeFileException();
    }
  }

  public void validateVideo(MultipartFile file) {
    if (getFileSizeMegaBytes(file) > 2000) {
      throw new FileExceptions.TooLargeFileException();
    }
  }

  private static long getFileSizeMegaBytes(MultipartFile file) {
    return file.getSize() / (1024 * 1024);
  }

  public static Optional<String> getExtensionByFilename(String filename) {
    return Optional.ofNullable(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1));
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
