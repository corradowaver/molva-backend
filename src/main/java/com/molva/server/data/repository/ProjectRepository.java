package com.molva.server.data.repository;

import com.molva.server.data.model.Profile;
import com.molva.server.data.model.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends CrudRepository<Project, Long> {
  Optional<List<Project>> findProjectsByProfile(Profile profile);

  Optional<Project> findProjectByName(String name);
}
