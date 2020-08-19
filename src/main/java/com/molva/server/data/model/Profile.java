package com.molva.server.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

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

  @OneToOne(mappedBy = "photoOwner")
  @JsonIgnore
  @ToString.Exclude
  private MediaFile photo;

  @OneToOne
  @JoinColumn(name = "application_user_fk")
  private ApplicationUser applicationUser;

  public Profile() {
  }

  public Profile(String firstname, String lastname) {
    this.firstname = firstname;
    this.lastname = lastname;
  }

}
