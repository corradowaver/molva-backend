package com.molva.server.data.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "project")
public @Data
class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", unique = true, nullable = false)
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "media")
  private String media;

  @ManyToOne
  @JoinColumn(name = "application_user_fk")
  private ApplicationUser applicationUser;

  public Project() {

  }

  public Project(String name, String description, String media) {
    this.name = name;
    this.description = description;
    this.media = media;
  }

}
