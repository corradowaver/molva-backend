package com.molva.server.data.service;

import com.molva.server.data.exceptions.project.ProjectExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import com.molva.server.data.model.Project;
import com.molva.server.data.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
  private final ProjectRepository projectRepository;

  @Autowired
  ProjectService(ProjectRepository projectRepository) {
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
