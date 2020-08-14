package com.molva.server.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Set;

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

  @OneToOne(mappedBy = "project")
  @JsonIgnore
  private MediaFile preview;

  @OneToMany(mappedBy = "filesProject", cascade = CascadeType.REMOVE)
  @JsonIgnore
  private Set<MediaFile> files;

  @ManyToOne
  @JoinColumn(name = "application_user_fk")
  @JsonIgnore
  private ApplicationUser applicationUser;

  public Project() {

  }

  public Project(String name, String description) {
    this.name = name;
    this.description = description;
  }

}
