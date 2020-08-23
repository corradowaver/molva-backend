package com.molva.server.data.service;

import com.molva.server.data.exceptions.project.ProjectExceptions;
import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.MediaFile;
import com.molva.server.data.model.Project;
import com.molva.server.data.repository.ApplicationUserRepository;
import com.molva.server.data.repository.MediaFileRepository;
import com.molva.server.data.repository.ProjectRepository;
import com.molva.server.helpers.ApplicationUserFactory;
import com.molva.server.helpers.MediaFileFactory;
import com.molva.server.helpers.ProjectFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;

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

  @MockBean
  private MediaFileRepository mediaFileRepository;

  @MockBean
  private ApplicationUserRepository applicationUserRepository;

  @Autowired
  ProjectFactory projectFactory;

  @Autowired
  ApplicationUserFactory userFactory;

  @Autowired
  MediaFileFactory mediaFileFactory;

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
    ApplicationUser applicationUser = userFactory.createApplicationUser();
    MediaFile mediaFile = mediaFileFactory.createMediaFile();
    mediaFile.setId(1L);
    applicationUser.setId(2L);
    project.setId(3L);

    doReturn(Optional.of(project)).when(repository).findById(any(Long.class));
    doReturn(project).when(repository).save(any(Project.class));
    doReturn(Optional.of(applicationUser)).when(applicationUserRepository).findById(any(Long.class));
    doReturn(applicationUser).when(applicationUserRepository).save(any(ApplicationUser.class));
    doReturn(mediaFile).when(mediaFileRepository).save(any(MediaFile.class));
    doReturn(Optional.of(mediaFile)).when(mediaFileRepository).findById(any(Long.class));

    MultipartFile preview = mediaFileFactory.createMultipartFile();
    service.addProject(project, applicationUser, preview);
    verify(repository, times(1)).save(project);
  }

  @Test
  void updateProjectById() {
    Project project = projectFactory.createProject();
    MultipartFile preview = mediaFileFactory.createMultipartFile();
    MediaFile mediaFile = mediaFileFactory.createMediaFile();
    ApplicationUser applicationUser = userFactory.createApplicationUser();
    mediaFile.setId(1L);
    project.setId(2L);
    applicationUser.setId(3L);

    doReturn(Optional.of(project)).when(repository).findById(any(Long.class));
    doReturn(project).when(repository).save(any(Project.class));
    doReturn(Optional.of(applicationUser)).when(applicationUserRepository).findById(any(Long.class));
    doReturn(mediaFile).when(mediaFileRepository).save(any(MediaFile.class));
    doReturn(Optional.of(mediaFile)).when(mediaFileRepository).findById(any(Long.class));

    Project addedProject = service.addProject(project, applicationUser, preview);
    service.updateProjectById(any(Long.class), addedProject, preview, null);
    verify(repository, times(2)).save(any(Project.class));
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
  void addProjectMustFailIfProjectExists() {
    Project existingProject = projectFactory.createProject();
    MultipartFile preview = mediaFileFactory.createMultipartFile();
    MediaFile mediaFile = mediaFileFactory.createMediaFile();
    ApplicationUser applicationUser = userFactory.createApplicationUser();

    doReturn(Optional.of(existingProject)).when(repository).findProjectByName(any(String.class));
    doReturn(mediaFile).when(mediaFileRepository).save(any(MediaFile.class));
    doReturn(Optional.of(mediaFile)).when(mediaFileRepository).findById(any(Long.class));
    assertThrows(ProjectExceptions.ProjectExistsException.class,
        () -> service.addProject(existingProject, applicationUser, preview));
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
    MultipartFile preview = mediaFileFactory.createMultipartFile();
    assertThrows(ProjectExceptions.ProjectNotFoundException.class,
        () -> service.updateProjectById(any(Long.class), project, preview, null));
  }

  @Test
  void deleteProjectByIdMustFailIfNotFound() {
    doReturn(Optional.empty()).when(repository).findById(any(Long.class));
    assertThrows(ProjectExceptions.ProjectNotFoundException.class,
        () -> service.deleteProjectById(any(Long.class)));
  }

}
