package com.molva.server.helpers;

import com.molva.server.data.model.Project;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
  Factory class, probably it's a temporal solution. Until we learn the best one.
 */
@Configuration
public class ProjectFactory {
  public Project createProject() {
    return new Project("Molva", "Web-site");
  }

  public List<Project> createProjectsList() {
    return List.of(createProject(), createProject(), createProject());
  }
}
