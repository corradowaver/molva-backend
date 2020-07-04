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

  @Column(name = "email")
  private String email;

  @Column(name = "photo")
  private String photo;

  @OneToMany(mappedBy = "profile", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
  private Set<Project> projects;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "application_user_fk", referencedColumnName = "id")
  private ApplicationUser applicationUser;

  public Profile() {
  }

  public Profile(String firstname, String lastname, String email, String photo) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
    this.photo = photo;
  }

}
