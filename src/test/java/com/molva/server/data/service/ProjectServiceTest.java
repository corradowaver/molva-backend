package com.molva.server.data.service;

import com.molva.server.data.exceptions.project.ProjectExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Project;
import com.molva.server.data.repository.ProjectRepository;
import com.molva.server.helpers.ApplicationUserFactory;
import com.molva.server.helpers.ProjectFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProjectServiceTest {

  @Autowired
  ProjectService service;

  @MockBean
  private ProjectRepository repository;

  @Autowired
  ProjectFactory projectFactory;

  @Autowired
  ApplicationUserFactory userFactory;

  @Test
  void loadAllProjects() {
    List<Project> projects = projectFactory.createProjectsList();
    doReturn(projects).when(repository).findAll();
    List<Project> returnedProjects = service.loadAllProjects();
    assertEquals(returnedProjects, projects);
  }

  @Test
  void loadProjectById() {
    Project project = projectFactory.createProject();
    doReturn(Optional.of(project)).when(repository).findById(any(Long.class));
    Project returnedProject = service.loadProjectById(any(Long.class));
    assertEquals(returnedProject, project);
  }

  @Test
  void loadAllProjectsByApplicationUser() {
    ApplicationUser user = userFactory.createApplicationUser();
    List<Project> projects = projectFactory.createProjectsList();
    doReturn(Optional.of(projects)).when(repository).findAllByApplicationUser(any(ApplicationUser.class));
    List<Project> returnedProjects = service.loadAllProjectsByApplicationUser(user);
    assertEquals(returnedProjects, projects);
  }

  @Test
  void addProject() {
    Project project = projectFactory.createProject();
    doReturn(project).when(repository).save(any(Project.class));
    service.addProject(project);
    verify(repository, times(1)).save(project);
  }

  @Test
  void updateProjectById() {
    Project project = projectFactory.createProject();
    doReturn(Optional.of(project)).when(repository).findById(any(Long.class));
    doReturn(project).when(repository).save(any(Project.class));
    service.updateProjectById(any(Long.class), project);
    verify(repository, times(1)).save(any(Project.class));
  }

  @Test
  void deleteProjectById() {
    Project project = projectFactory.createProject();
    doReturn(Optional.of(project)).when(repository).findById(any(Long.class));
    doNothing().when(repository).deleteById(any(Long.class));
    service.deleteProjectById(any(Long.class));
    verify(repository, times(1)).deleteById(any(Long.class));
  }

  @Test
  void addProjectMustFailIfProfileExists() {
    Project existingProject = projectFactory.createProject();
    doReturn(Optional.of(existingProject)).when(repository).findProjectByName(any(String.class));
    assertThrows(ProjectExceptions.ProjectExistsException.class,
        () -> service.addProject(existingProject));
  }

  @Test
  void loadProjectByIdMustFailIfNotFound() {
    doReturn(Optional.empty()).when(repository).findById(any(Long.class));
    assertThrows(ProjectExceptions.ProjectNotFoundException.class,
        () -> service.loadProjectById(any(Long.class)));
  }

  @Test
  void loadAllProjectByApplicationUserMustFailIfNotFound() {
    doReturn(Optional.empty()).when(repository).findAllByApplicationUser(any(ApplicationUser.class));
    assertThrows(ProjectExceptions.ProjectNotFoundException.class,
        () -> service.loadAllProjectsByApplicationUser(any(ApplicationUser.class)));
  }

  @Test
  void updateProjectByIdMustFailIfNotFound() {
    Project project = projectFactory.createProject();
    doReturn(Optional.empty()).when(repository).findById(any(Long.class));
    assertThrows(ProjectExceptions.ProjectNotFoundException.class,
        () -> service.updateProjectById(any(Long.class), project));
  }

  @Test
  void deleteProjectByIdMustFailIfNotFound() {
    doReturn(Optional.empty()).when(repository).findById(any(Long.class));
    assertThrows(ProjectExceptions.ProjectNotFoundException.class,
        () -> service.deleteProjectById(any(Long.class)));
  }

}
