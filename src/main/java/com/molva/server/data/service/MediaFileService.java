package com.molva.server.data.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.molva.server.data.exceptions.file.FileExceptions;
import com.molva.server.data.model.ProjectFile;
import com.molva.server.data.model.ProjectPreview;
import com.molva.server.data.repository.ProjectFileRepository;
import com.molva.server.data.repository.ProjectPreviewRepository;
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
  private final ProjectPreviewRepository projectPreviewRepository;
  private final ProjectFileRepository projectFileRepository;

  @Value("${gcs.bucket.name}")
  private String bucketName;

  Storage storage;

  static final String FILE_PATTERN = "file:";

  @Autowired
  MediaFileService(ProjectPreviewRepository projectPreviewRepository,
                   ProjectFileRepository projectFileRepository,
                   @Value("${spring.cloud.gcp.credentials.location}") String rawCredentialsPath) throws IOException {
    this.projectPreviewRepository = projectPreviewRepository;
    this.projectFileRepository = projectFileRepository;
    String credentialsPath = rawCredentialsPath.replaceFirst(Pattern.quote(FILE_PATTERN), "");
    Credentials credentials = GoogleCredentials.fromStream(
        new FileInputStream(credentialsPath)
    );
    storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
  }

  public ProjectPreview loadProjectPreviewById(Long id) {
    return projectPreviewRepository
        .findById(id)
        .orElseThrow(FileExceptions.FileNotFoundException::new);
  }

  public ProjectFile loadProjectFileById(Long id) {
    return projectFileRepository
        .findById(id)
        .orElseThrow(FileExceptions.FileNotFoundException::new);
  }

  public byte[] loadProjectPreviewBytesById(Long id) {
    return getMediaFileBytes(loadProjectPreviewById(id).getPath());
  }

  public byte[] loadProjectFileBytesById(Long id) {
    return getMediaFileBytes(loadProjectFileById(id).getPath());
  }

  public ProjectFile addProjectFile(MultipartFile multipartFile) {
    if (multipartFile == null) return null;
    File convertedFile = FileConverters.convertMultiPartToFile(multipartFile);
    ProjectFile savedFile = saveProjectFile(convertedFile);
    try {
      saveFileToStorage(convertedFile, savedFile.getPath());
      return savedFile;
    } catch (IOException ex) {
      deleteProjectPreviewById(savedFile.getId());
      throw new FileExceptions.InvalidFileException();
    }
  }

  public ProjectPreview addProjectPreview(MultipartFile multipartFile) {
    if (multipartFile == null) return null;
    File convertedFile = FileConverters.convertMultiPartToFile(multipartFile);
    ProjectPreview savedPreview = saveProjectPreview(convertedFile);
    try {
      saveFileToStorage(convertedFile, savedPreview.getPath());
      convertedFile.delete();
      return savedPreview;
    } catch (IOException ex) {
      deleteProjectPreviewById(savedPreview.getId());
      convertedFile.delete();
      throw new FileExceptions.InvalidFileException();
    }
  }

  public ProjectFile saveProjectFile(File convertedFile) {
    try (InputStream is = new FileInputStream(convertedFile)) {
      BasicFileAttributes basicFileAttributes = Files.readAttributes(
          Paths.get(convertedFile.getAbsolutePath()), BasicFileAttributes.class
      );
      FileNameMap fileNameMap = URLConnection.getFileNameMap();
      String mimeType = fileNameMap.getContentTypeFor(convertedFile.getName());
      ProjectFile mediaFile = new ProjectFile(
          new Date(basicFileAttributes.creationTime().toMillis()),
          new Date(basicFileAttributes.lastModifiedTime().toMillis()),
          DigestUtils.md5Hex(is),
          mimeType,
          basicFileAttributes.size()
      );
      mediaFile = projectFileRepository.save(mediaFile);
      String filename = getFilePath(mediaFile.getId()) + "."
          + FileValidators.getExtensionByFilename
          (
              convertedFile.getName()
          ).orElseThrow(FileExceptions.InvalidFileException::new);
      mediaFile.setPath(filename);
      return projectFileRepository.save(mediaFile);
    } catch (IOException e) {
      convertedFile.delete();
      throw new FileExceptions.InvalidFileException();
    }
  }

  public ProjectPreview saveProjectPreview(File convertedFile) {
    try (InputStream is = new FileInputStream(convertedFile)) {
      BasicFileAttributes basicFileAttributes = Files.readAttributes(
          Paths.get(convertedFile.getAbsolutePath()), BasicFileAttributes.class
      );
      FileNameMap fileNameMap = URLConnection.getFileNameMap();
      String mimeType = fileNameMap.getContentTypeFor(convertedFile.getName());
      ProjectPreview mediaFile = new ProjectPreview(
          new Date(basicFileAttributes.creationTime().toMillis()),
          new Date(basicFileAttributes.lastModifiedTime().toMillis()),
          DigestUtils.md5Hex(is),
          mimeType,
          basicFileAttributes.size()
      );
      mediaFile = projectPreviewRepository.save(mediaFile);
      String filename = getFilePath(mediaFile.getId()) + "."
          + FileValidators.getExtensionByFilename
          (
              convertedFile.getName()
          ).orElseThrow(FileExceptions.InvalidFileException::new);
      mediaFile.setPath(filename);
      return projectPreviewRepository.save(mediaFile);
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

  public void deleteProjectPreviewById(Long id) {
    String path = loadProjectPreviewById(id).getPath();
    storage.delete(BlobId.of(bucketName, path));
    projectPreviewRepository.deleteById(id);
  }

  public void deleteProjectFileById(Long id) {
    String path = loadProjectFileById(id).getPath();
    storage.delete(BlobId.of(bucketName, path));
    projectFileRepository.deleteById(id);
  }

  public ProjectPreview updateProjectPreviewById(Long id, ProjectPreview projectPreview) {
    Optional<ProjectPreview> mediaFileOptional = projectPreviewRepository.findById(id);
    if (mediaFileOptional.isPresent()) {
      projectPreview.setId(mediaFileOptional.get().getId());
      return projectPreviewRepository.save(projectPreview);
    } else {
      throw new FileExceptions.FileNotFoundException();
    }
  }

  public ProjectFile updateProjectFileById(Long id, ProjectFile projectFile) {
    Optional<ProjectFile> mediaFileOptional = projectFileRepository.findById(id);
    if (mediaFileOptional.isPresent()) {
      projectFile.setId(mediaFileOptional.get().getId());
      return projectFileRepository.save(projectFile);
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
