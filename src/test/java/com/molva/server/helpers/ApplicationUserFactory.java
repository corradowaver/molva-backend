package com.molva.server.helpers;

import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
  Factory class, probably it's a temporal solution. Until we learn the best one.
 */
@Configuration
public class ApplicationUserFactory {
  public ApplicationUser createApplicationUser() {
    return new ApplicationUser("username", "password");
  }

  public List<ApplicationUser> createApplicationUsers() {
    return List.of(createApplicationUser(), createApplicationUser(), createApplicationUser());
  }
}
