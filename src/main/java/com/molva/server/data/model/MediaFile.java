package com.molva.server.data.model;

import lombok.Data;

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

  @JoinTable(name = "preview_project",
      joinColumns = @JoinColumn(name = "preview_file_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id")
  )
  @OneToOne()
  private Project previewOwner;

  @JoinTable(
      name = "file_project",
      joinColumns = @JoinColumn(name = "project_file_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id")
  )
  @ManyToOne()
  private Project fileOwner;

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

  public MediaFile(Date created, Date updated, String md5, String mime, long size) {
    this.created = created;
    this.updated = updated;
    this.md5 = md5;
    this.mime = mime;
    this.size = size;
  }
}