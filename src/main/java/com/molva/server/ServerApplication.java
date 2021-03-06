package com.molva.server;

import com.molva.server.data.model.ApplicationUser;
import com.molva.server.data.model.Profile;
import com.molva.server.data.service.ApplicationUserService;
import com.molva.server.data.service.ProfileService;
import com.molva.server.security.roles.ApplicationUserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@SpringBootApplication
public class ServerApplication {
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(ServerApplication.class);
    app.setDefaultProperties(Collections
        .singletonMap("server.port", "9001"));
    app.run(args);
  }

  @Bean
  public CommandLineRunner test(ApplicationUserService applicationUserService, ProfileService profileService) {
    return args -> {
//      ApplicationUser admin = applicationUserService.registerUser(
//          new ApplicationUser("admin", "123456l", "majorwombat@rainbow.com"),
//          ApplicationUserRole.ADMIN);
//      Profile profile = new Profile();
//      profile.setApplicationUser(admin);
//      Profile p = profileService.addProfile(profile, admin);
//      admin.setProfile(p);
//      applicationUserService.updateApplicationUserById(admin.getId(), admin);
    };
  }

}
