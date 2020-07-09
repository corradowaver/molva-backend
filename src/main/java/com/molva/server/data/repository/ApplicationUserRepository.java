package com.molva.server.data.repository;

import com.molva.server.data.model.ApplicationUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface ApplicationUserRepository extends CrudRepository<ApplicationUser, Long> {
  Optional<UserDetails> findUserByUsername(String username);
  Optional<ApplicationUser> findAccountByUsername(String username);
  Optional<ApplicationUser> findAccountByEmail(String email);
}
