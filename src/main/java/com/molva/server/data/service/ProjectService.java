package com.molva.server.data.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.molva.server.data.exceptions.project.ProjectExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Project;
import com.molva.server.data.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
  private final ProjectRepository projectRepository;

  @Autowired
  ProjectService(ProjectRepository projectRepository) throws IOException {
    this.projectRepository = projectRepository;
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


//  public String deleteProjectPreviewFromStorage(String filename) {
//    storage.delete(BlobId.of(bucketName, "project_previews/" +
//        filename));
//    return "Success";
//  }
//
//  public byte[] getProjectPreview(String filename) {
//    Blob blob = storage.get(bucketName, "project_previews/" + filename);
//    return blob.getContent();
//  }

  public void validateProjectName(String name) {
    if (name.length() >= 66 || name.length() < 3) {
      throw new ProjectExceptions.ProjectInvalidNameException();
    }
  }

  public void validateProjectDescription(String description) {
    if (description.split(" ").length > 2000 || description.length() > 15000) {
      throw new ProjectExceptions.ProjectInvalidDescriptionException();
    }
  }

  public Project addProject(Project project) {
    Optional<Project> projectWithTheSameName =
        projectRepository.findProjectByName(project.getName());
    if (projectWithTheSameName.isPresent()) {
      throw new ProjectExceptions.ProjectExistsException();
    }
    return projectRepository.save(project);
  }

  public Project updateProjectById(Long id, Project newProject) {
    Optional<Project> projectOptional = projectRepository.findById(id);
    if (projectOptional.isPresent()) {
      newProject.setId(projectOptional.get().getId());
      return projectRepository.save(newProject);
    } else {
      throw new ProjectExceptions.ProjectNotFoundException();
    }
  }

  public void deleteProjectById(Long id) {
    Optional<Project> projectOptional = projectRepository.findById(id);
    if (projectOptional.isPresent()) {
      projectRepository.deleteById(id);
    } else {
      throw new ProjectExceptions.ProjectNotFoundException();
    }
  }
}
