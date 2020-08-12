package com.molva.server.helpers;

import com.molva.server.data.model.ApplicationUser;
import com.molva.server.security.roles.ApplicationUserRole;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
  Factory class, probably it's a temporal solution. Until we learn the best one.
 */
@Configuration
public class ApplicationUserFactory {
  public ApplicationUser createApplicationUser() {
    return new ApplicationUser("username", "password", "email@mail.com");
  }

  public List<ApplicationUser> createApplicationUsers() {
    return List.of(createApplicationUser(), createApplicationUser(), createApplicationUser());
  }

  public ApplicationUser createRegisteredModerator(long id) {
    var moderator = new ApplicationUser("username", "password", "email@mail.com",
        ApplicationUserRole.MODERATOR, null, true, true, true, true);
    moderator.setId(id);
    return moderator;
  }
}
