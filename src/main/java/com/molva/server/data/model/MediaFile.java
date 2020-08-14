package com.molva.server.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "media_file")
public @Data
class MediaFile {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", unique = true, nullable = false)
  private Long id;

  @OneToOne()
  @JsonIgnore
  @EqualsAndHashCode.Exclude
  private Project project;

  @ManyToOne()
  @JsonIgnore
  @EqualsAndHashCode.Exclude
  private Project filesProject;

  @Column(name = "created", nullable = false)
  @Temporal(TemporalType.DATE)
  private Date created;
  @Column(name = "updated")
  @Temporal(TemporalType.DATE)
  private Date updated;

  @Column(name = "path")
  private String path;
  @Column(name = "mime", nullable = false)
  private String mime;
  @Column(name = "md5", nullable = false)
  private String md5;
  @Column(name = "size", nullable = false)
  private long size;

  public MediaFile() {
  }
}
