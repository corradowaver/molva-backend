package com.molva.server.helpers;

import com.molva.server.data.model.Profile;
import org.springframework.context.annotation.Configuration;

/*
  Factory class, probably it's a temporal solution. Until we learn the best one.
 */
@Configuration
public class ProfileFactory {
  public Profile createProfile() {
    return new Profile("First", "Profile", "email", "photo");
  }
}
