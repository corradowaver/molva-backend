package com.molva.server.data.model;

import javax.persistence.*;

@Entity
@Table(name = "project")
public class Project {

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
  @JoinColumn(name = "profile_fk")
  private Profile profile;

  public Project() {

  }

  public Project(Long id, String name, String description, String media) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.media = media;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getMedia() {
    return media;
  }

  public void setMedia(String media) {
    this.media = media;
  }
}
