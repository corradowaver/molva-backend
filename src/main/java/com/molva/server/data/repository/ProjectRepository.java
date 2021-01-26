package com.molva.server.data.repository;

import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends CrudRepository<Project, Long> {
  Optional<Project> findProjectByName(String name);
  Optional<List<Project>> findAllByApplicationUser(ApplicationUser user);
}
