package com.molva.server.controller.management;

import com.molva.server.data.exceptions.file.FileExceptions;
import com.molva.server.data.exceptions.profile.ProfileExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Project;
import com.molva.server.data.service.ApplicationUserService;
import com.molva.server.data.service.ProjectService;
import com.molva.server.security.jwt.JwtProvider;
import com.sun.istack.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

//This is v1 of ProjectController, file upload might change
@RestController
@RequestMapping("management/api/v1/profile")
public class ProjectController {

  private final ProjectService projectService;
  private final ApplicationUserService applicationUserService;
  private final JwtProvider jwtProvider;

  public ProjectController(ProjectService projectService, ApplicationUserService applicationUserService, JwtProvider jwtProvider) {
    this.projectService = projectService;
    this.applicationUserService = applicationUserService;
    this.jwtProvider = jwtProvider;
  }

  @PostMapping(path = "/add/project")
  @PreAuthorize("hasAuthority('moderator:write')")
  public Project addProject(@RequestHeader("Authorization") String token,
                            @RequestBody Project project,
                            @RequestPart String description,
                            @RequestPart("file") MultipartFile file) {
    try {
      Project addedProject = projectService.addProject(project);
      URL url = saveProjectPreview(file, description);
      ApplicationUser account = applicationUserService.loadAccountByUsername(jwtProvider.getUsername(token));
      addedProject.setApplicationUser(account);
      addedProject.setMedia(url.toString());
      return projectService.updateProjectById(addedProject.getId(), addedProject);
    } catch (IOException ex) {
      throw new FileExceptions.InvalidFileException();
    }
  }

  @PutMapping(path = "/edit/project/{projectId}")
  @PreAuthorize("hasAuthority('moderator:write')")
  public Project updateProject(@NotNull @PathVariable("projectId") Long projectId,
                               @RequestHeader("Authorization") String token,
                               @RequestBody Project project,
                               @RequestPart String description,
                               @RequestPart("file") MultipartFile file) {
    try {
      checkIfProjectIdMatches(token, projectId);
      Project updatedProject = projectService.loadProjectById(projectId);
      // TODO: 03.08.2020 add deletion of an old project preview
      URL url = saveProjectPreview(file, description);
      updatedProject.setMedia(url.toString());
      updatedProject.setName(project.getName());
      updatedProject.setDescription(project.getDescription());
      return projectService.updateProjectById(projectId, updatedProject);
    } catch (IOException ex) {
      throw new FileExceptions.InvalidFileException();
    }
  }

  @DeleteMapping(path = "/delete/project/{projectId}")
  public void deleteProject(@RequestHeader("Authorization") String token,
                            @NotNull @PathVariable("projectId") Long projectId) {
    checkIfProjectIdMatches(token, projectId);
    projectService.deleteProjectById(projectId);
  }

  private URL saveProjectPreview(MultipartFile file, String description) throws MalformedURLException {
    // TODO: 03.08.2020 save file to the remote storage and return URL
    return new URL("");
  }

  private void checkIfProjectIdMatches(String token, Long id) {
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
