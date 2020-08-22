package com.molva.server.data.service;

import com.molva.server.data.exceptions.project.ProjectExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.MediaFile;
import com.molva.server.data.model.Project;
import com.molva.server.data.repository.ProjectRepository;
import com.molva.server.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {
  private final ProjectRepository projectRepository;
  private final MediaFileService mediaFileService;
  private final ApplicationUserService applicationUserService;
  private final JwtProvider jwtProvider;

  @Autowired
  ProjectService(
      ProjectRepository projectRepository,
      MediaFileService mediaFileService,
      ApplicationUserService applicationUserService,
      JwtProvider jwtProvider
  ) {
    this.projectRepository = projectRepository;
    this.mediaFileService = mediaFileService;
    this.applicationUserService = applicationUserService;
    this.jwtProvider = jwtProvider;
  }

  public List<Project> loadAllProjects() {
    return (List<Project>) projectRepository.findAll();
  }

  public Project loadProjectById(Long id) {
    return projectRepository
        .findById(id)
        .orElseThrow(ProjectExceptions.ProjectNotFoundException::new);
  }

  public List<Project> loadAllProjectsByApplicationUser(ApplicationUser user) {
    return projectRepository
        .findAllByApplicationUser(user)
        .orElseThrow(ProjectExceptions.ProjectNotFoundException::new);
  }

  public Project addProject(Project project, String token, MultipartFile preview) {
    ApplicationUser applicationUser = applicationUserService.loadAccountByUsername(
        jwtProvider.getUsername(jwtProvider.resolveToken(token))
    );
    project.setApplicationUser(applicationUser);
    Project savedProject = saveProjectIfThereIsNoneWithTheSameName(project);
    addProjectToUserProjects(applicationUser, savedProject);
    savedProject.setPreview(saveSingleFileToStorage(preview));
    updateProjectById(savedProject.getId(), savedProject, preview, null);
    setFileOwnerProjectForSingleFile(savedProject.getPreview(), savedProject);
    return projectRepository.save(project);
  }

  public Project updateProjectById(
      Long id,
      Project newProject,
      MultipartFile preview,
      MultipartFile[] files
  ) {
    Project project = loadProjectById(id);
    project.setName(newProject.getName());
    project.setDescription(newProject.getDescription());
    deleteOldFileIfItExists(project.getPreview());
    project.setPreview(saveSingleFileToStorage(preview));
    if (files != null) {
      project.getFiles().forEach(this::deleteOldFileIfItExists);
      project.setFiles(saveAllFilesToStorage(files));
    }
    project.getPreview().setPreviewOwner(project);
    setFileOwnerProjectForSingleFile(project.getPreview(), project);
    setFileOwnerProjectForEachFile(project.getFiles(), project);
    return projectRepository.save(project);
  }

  public void deleteProjectById(Long id) {
    Project project = loadProjectById(id);
    deleteOldFileIfItExists(project.getPreview());
    if (project.getFiles() != null) {
      project.getFiles().forEach(this::deleteOldFileIfItExists);
    }
    projectRepository.deleteById(id);
  }

  public void deleteOldFileIfItExists(MediaFile file) {
    if (file != null) {
      mediaFileService.deleteMediaFileById(file.getId());
    }
  }

  private void addProjectToUserProjects(ApplicationUser applicationUser, Project project) {
    applicationUser.addProject(project);
    applicationUserService.updateApplicationUserById(applicationUser.getId(), applicationUser);
  }

  private Project saveProjectIfThereIsNoneWithTheSameName(Project project) {
    Optional<Project> projectWithTheSameName =
        projectRepository.findProjectByName(project.getName());
    if (projectWithTheSameName.isPresent()) {
      throw new ProjectExceptions.ProjectExistsException();
    }
    return projectRepository.save(project);
  }

  private Set<MediaFile> saveAllFilesToStorage(MultipartFile[] files) {
    return Arrays.stream(files)
        .filter(Objects::nonNull)
        .map(mediaFileService::addMediaFile)
        .collect(Collectors.toSet());
  }

  private void setFileOwnerProjectForEachFile(Set<MediaFile> mediaFiles, Project owner) {
    mediaFiles.forEach(file -> {
      file.setFileOwner(owner);
      mediaFileService.updateMediaFileById(file.getId(), file);
    });
  }

  private void setFileOwnerProjectForSingleFile(MediaFile mediaFile, Project owner) {
    mediaFile.setPreviewOwner(owner);
    mediaFileService.updateMediaFileById(mediaFile.getId(), owner.getPreview());
  }

  private MediaFile saveSingleFileToStorage(MultipartFile file) {
    return mediaFileService.addMediaFile(file);
  }
}
