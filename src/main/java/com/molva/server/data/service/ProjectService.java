package com.molva.server.data.service;

import com.molva.server.data.exceptions.file.FileExceptions;
import com.molva.server.data.exceptions.profile.ProfileExceptions;
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

  public Project addProject(Project project, ApplicationUser applicationUser, MultipartFile preview) {
    throwAnExceptionIfThereIsProjectWithTheSameName(project);
    project.setApplicationUser(applicationUser);
    project.setPreview(saveSingleFileToStorage(preview));
    try {
      Project savedProject = projectRepository.save(project);
      addProjectToUserProjects(applicationUser, savedProject);
      setFileOwnerProjectForSingleFile(savedProject.getPreview(), savedProject);
      return savedProject;
    } catch (ProfileExceptions.ProfileNotFoundException
        | FileExceptions.FileNotFoundException ex) {
      mediaFileService.deleteMediaFileById(project.getPreview().getId());
      throw ex;
    }
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
    throwAnExceptionIfThereIsProjectWithTheSameName(project);
    if (preview != null) {
      deleteOldFileIfItExists(project.getPreview());
      project.setPreview(saveSingleFileToStorage(preview));
      setFileOwnerProjectForSingleFile(project.getPreview(), project);
    }
    if (files != null) {
      project.getFiles().forEach(this::deleteOldFileIfItExists);
      project.setFiles(saveAllFilesToStorage(files));
      setFileOwnerProjectForEachFile(project.getFiles(), project);
    }
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

  private void throwAnExceptionIfThereIsProjectWithTheSameName(Project project) {
    Optional<Project> projectWithTheSameName =
        projectRepository.findProjectByName(project.getName());
    if (projectWithTheSameName.isPresent()) {
      throw new ProjectExceptions.ProjectExistsException();
    }
  }

  private Set<MediaFile> saveAllFilesToStorage(MultipartFile[] files) {
    return Arrays.stream(files)
        .filter(Objects::nonNull)
        .map(mediaFileService::addMediaFile)
        .collect(Collectors.toSet());
  }

  private void setFileOwnerProjectForEachFile(Set<MediaFile> mediaFiles, Project owner) {
    if (mediaFiles != null) {
      mediaFiles.forEach(file -> {
        file.setFileOwner(owner);
        mediaFileService.updateMediaFileById(file.getId(), file);
      });
    }
  }

  private void setFileOwnerProjectForSingleFile(MediaFile mediaFile, Project owner) {
    mediaFile.setPreviewOwner(owner);
    mediaFileService.updateMediaFileById(mediaFile.getId(), owner.getPreview());
  }

  private MediaFile saveSingleFileToStorage(MultipartFile file) {
    return mediaFileService.addMediaFile(file);
  }
}
