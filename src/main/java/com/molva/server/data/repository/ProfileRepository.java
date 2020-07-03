package com.molva.server.data.repository;

import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProfileRepository extends CrudRepository<Profile, Long> {
  Optional<Profile> findProfileByApplicationUser(ApplicationUser applicationUser);
}
