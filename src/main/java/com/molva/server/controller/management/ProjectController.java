package com.molva.server.controller.management;

import com.molva.server.data.exceptions.profile.ProfileExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Project;
import com.molva.server.data.service.ApplicationUserService;
import com.molva.server.data.service.ProjectService;
import com.molva.server.data.service.helpers.FileValidators;
import com.molva.server.data.service.helpers.ProjectValidators;
import com.molva.server.security.jwt.JwtProvider;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@RestController
@RequestMapping("management/api/v1/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final ApplicationUserService applicationUserService;
  private final JwtProvider jwtProvider;

  @Autowired
  public ProjectController(
      ProjectService projectService,
      ApplicationUserService applicationUserService,
      JwtProvider jwtProvider
  ) {
    this.projectService = projectService;
    this.applicationUserService = applicationUserService;
    this.jwtProvider = jwtProvider;
  }

  @PostMapping(path = "/add")
  @PreAuthorize("hasAuthority('moderator:write')")
  public Project addProject(@RequestHeader("Authorization") String token,
                            Project project,
                            @RequestParam("preview-multipart") MultipartFile preview) {
    validateProjectData(project, preview, null);
    ApplicationUser applicationUser = applicationUserService.loadAccountByUsername(
        jwtProvider.getUsername(jwtProvider.resolveToken(token))
    );
    return projectService.addProject(project, applicationUser, preview);
  }

  @PutMapping(path = "/edit/{projectId}")
  @PreAuthorize("hasAuthority('moderator:write')")
  public Project updateProject(@NotNull @PathVariable("projectId") Long projectId,
                               @RequestHeader("Authorization") String token,
                               Project projectData,
                               @RequestParam(value = "preview-multipart", required = false) MultipartFile preview,
                               @RequestParam(value = "files-multipart", required = false) MultipartFile[] files) {
    throwAnExceptionIfProjectIdNotMatchesUserProjects(token, projectId);
    validateProjectData(projectData, preview, files);
    return projectService.updateProjectById(projectId, projectData, preview, files);
  }

  @DeleteMapping(path = "/delete/{projectId}")
  public void deleteProject(@RequestHeader("Authorization") String token,
                            @NotNull @PathVariable("projectId") Long projectId) {
    throwAnExceptionIfProjectIdNotMatchesUserProjects(token, projectId);
    projectService.deleteProjectById(projectId);
  }

  public void validateProjectData(
      Project projectData,
      MultipartFile preview,
      MultipartFile[] files
  ) {
    ProjectValidators.validateProjectName(projectData.getName());
    ProjectValidators.validateProjectDescription(projectData.getDescription());
    if (preview != null) {
      FileValidators.validateMediaFile(preview);
    }
    if (files != null) {
      Arrays.stream(files).forEach(FileValidators::validateMediaFile);
    }
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
