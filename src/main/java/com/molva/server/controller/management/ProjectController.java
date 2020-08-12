package com.molva.server.controller.management;

import com.molva.server.data.exceptions.profile.ProfileExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.MediaFile;
import com.molva.server.data.model.Project;
import com.molva.server.data.service.ApplicationUserService;
import com.molva.server.data.service.MediaFileService;
import com.molva.server.data.service.ProjectService;
import com.molva.server.security.jwt.JwtProvider;
import com.sun.istack.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("management/api/v1/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final ApplicationUserService applicationUserService;
  private final MediaFileService mediaFileService;
  private final JwtProvider jwtProvider;

  public ProjectController(
      ProjectService projectService,
      ApplicationUserService applicationUserService,
      MediaFileService mediaFileService,
      JwtProvider jwtProvider
  ) {
    this.projectService = projectService;
    this.applicationUserService = applicationUserService;
    this.mediaFileService = mediaFileService;
    this.jwtProvider = jwtProvider;
  }

  @PostMapping(path = "/add")
  @PreAuthorize("hasAuthority('moderator:write')")
  public Project addProject(@RequestHeader("Authorization") String token,
                            Project project,
                            @RequestParam("preview") MultipartFile preview) {
    validateProjectData(project, preview, null);
    ApplicationUser applicationUser = applicationUserService.loadAccountByUsername(
        jwtProvider.getUsername(jwtProvider.resolveToken(token))
    );
    project.setApplicationUser(applicationUser);
    Project savedProject = projectService.addProject(project);
    applicationUser.addProject(savedProject);
    applicationUserService.updateApplicationUserById(applicationUser.getId(), applicationUser);
    MediaFile mediaFile = mediaFileService.addMediaFile(preview);
    savedProject.setPreview(mediaFile);
    projectService.updateProjectById(savedProject.getId(), savedProject);
    mediaFile.setProject(savedProject);
    mediaFileService.updateMediaFileById(mediaFile.getId(), mediaFile);
    return savedProject;
  }

  @PutMapping(path = "/edit/{projectId}")
  @PreAuthorize("hasAuthority('moderator:write')")
  public Project updateProject(@NotNull @PathVariable("projectId") Long projectId,
                               @RequestHeader("Authorization") String token,
                               Project projectData,
                               @RequestParam("preview") MultipartFile preview,
                               @RequestParam("files") MultipartFile[] files) {
    throwAnExceptionIfProjectIdNotMatchesUserProjects(token, projectId);
    validateProjectData(projectData, preview, files);
    Project project = projectService.loadProjectById(projectId);
    project.setName(projectData.getName());
    project.setDescription(projectData.getDescription());
    MediaFile oldPreview = project.getPreview();
    if (oldPreview != null) {
      mediaFileService.deleteMediaFileById(oldPreview.getId());
    }
    Set<MediaFile> oldFiles = project.getFiles();
    if (oldFiles != null) {
      oldFiles.forEach(oldFile -> {
        mediaFileService.deleteMediaFileById(oldFile.getId());
      });
    }
    MediaFile newPreview = mediaFileService.addMediaFile(preview);
    project.setPreview(newPreview);
    Set<MediaFile> newFiles = Arrays.stream(files)
        .filter(Objects::nonNull)
        .map(mediaFileService::addMediaFile)
        .collect(Collectors.toSet());
    project.setFiles(newFiles);
    projectService.updateProjectById(projectId, project);
    newPreview.setProject(project);
    mediaFileService.updateMediaFileById(newPreview.getId(), newPreview);
    newFiles.forEach(file -> {
      file.setProject(project);
      mediaFileService.updateMediaFileById(file.getId(), file);
    });
    return project;
  }

  public void validateProjectData(
      Project projectData,
      MultipartFile preview,
      MultipartFile[] files
  ) {
    projectService.validateProjectName(projectData.getName());
    projectService.validateProjectDescription(projectData.getDescription());
    mediaFileService.validateMediaFile(preview);
    if (files != null) {
      Arrays.stream(files).forEach(mediaFileService::validateMediaFile);
    }
  }

  @DeleteMapping(path = "/delete/{projectId}")
  public void deleteProject(@RequestHeader("Authorization") String token,
                            @NotNull @PathVariable("projectId") Long projectId) {
    throwAnExceptionIfProjectIdNotMatchesUserProjects(token, projectId);
    Project project = projectService.loadProjectById(projectId);
    mediaFileService.deleteMediaFileById(project.getPreview().getId());
    project.getFiles().forEach(file -> {
      mediaFileService.deleteMediaFileById(file.getId());
    });
    projectService.deleteProjectById(projectId);
  }

  private void throwAnExceptionIfProjectIdNotMatchesUserProjects(String token, Long id) {
    ApplicationUser applicationUser = applicationUserService.loadAccountByUsername(
        jwtProvider.getUsername(jwtProvider.resolveToken(token))
    );
    if (applicationUser
        .getProjects()
        .stream()
        .noneMatch((project) -> (project.getApplicationUser() == applicationUser) && (project.getId().equals(id)))) {
      throw new ProfileExceptions.ProfileNotFoundException();
    }
  }
}
