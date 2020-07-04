package com.molva.server.data.service;

import com.molva.server.data.model.Profile;
import com.molva.server.data.model.Project;
import com.molva.server.data.repository.ProjectRepository;
import com.molva.server.helpers.ProfileFactory;
import com.molva.server.helpers.ProjectFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
  ProfileFactory profileFactory;

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
  void loadAllProjectsByProfile() {
    Profile profile = profileFactory.createProfile();
    List<Project> projects = projectFactory.createProjectsList();
    doReturn(Optional.of(projects)).when(repository).findProjectsByProfile(any(Profile.class));
    List<Project> returnedProjects = service.loadAllProjectsByProfile(profile);
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

}
