package com.molva.server.data.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "profile")
public @Data
class Profile {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", unique = true, nullable = false)
  private Long id;

  @Column(name = "firstname")
  private String firstname;

  @Column(name = "lastname")
  private String lastname;

  @Column(name = "photo")
  private String photo;

  @OneToOne
  @JoinColumn(name = "application_user_fk")
  private ApplicationUser applicationUser;

  public Profile() {
  }

  public Profile(String firstname, String lastname, String photo) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.photo = photo;
  }

}
