package com.molva.server.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.molva.server.security.roles.ApplicationUserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Entity
@Table(name = "application_user")
public @Data
class ApplicationUser implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", unique = true, nullable = false)
  private Long id;

  @Column(name = "login")
  private String username;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  @JsonProperty(access = WRITE_ONLY)
  private String password;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
  @JsonIgnore
  @JoinColumn(name = "profile_fk")
  private Profile profile;

  @Column(name = "granted_authorities")
  private ApplicationUserRole applicationUserRole;

  @Column(name = "is_account_non_expired")
  private boolean isAccountNonExpired;

  @Column(name = "is_account_non_locked")
  private boolean isAccountNonLocked;

  @Column(name = "is_credentials_non_expired")
  private boolean isCredentialsNotExpired;

  @Column(name = "is_account_enabled")
  private boolean isEnabled;

  @OneToMany(mappedBy = "applicationUser", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
  @EqualsAndHashCode.Exclude
  private Set<Project> createdProjects;

  @JoinTable(
      name = "members_projects",
      joinColumns = @JoinColumn(name = "project_member_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id")
  )
  @ManyToMany(fetch = FetchType.EAGER)
  @EqualsAndHashCode.Exclude
  private Set<Project> joinedProjects;

  public ApplicationUser() {
    this.createdProjects = new HashSet<>();
    this.joinedProjects = new HashSet<>();
  }

  public ApplicationUser(String username,
                         String password,
                         String email,
                         ApplicationUserRole applicationUserRole,
                         Set<Project> createdProjects,
                         boolean isAccountNonExpired,
                         boolean isAccountNonLocked,
                         boolean isCredentialsNotExpired,
                         boolean isEnabled) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.applicationUserRole = applicationUserRole;
    this.createdProjects = createdProjects;
    this.joinedProjects = new HashSet<>();
    this.isAccountNonExpired = isAccountNonExpired;
    this.isAccountNonLocked = isAccountNonLocked;
    this.isCredentialsNotExpired = isCredentialsNotExpired;
    this.isEnabled = isEnabled;
  }

  public ApplicationUser(String username,
                         String password,
                         String email) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.createdProjects = new HashSet<>();
    this.joinedProjects = new HashSet<>();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return applicationUserRole.getGrantedAuthorities();
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public Set<Project> getCreatedProjects() {
    return createdProjects;
  }

  public void setCreatedProjects(Set<Project> projects) {
    this.createdProjects = projects;
  }

  public void addCreatedProject(Project project) {
    createdProjects.add(project);
  }

  public boolean addJoinedProject(Project project) {
    return joinedProjects.add(project);
  }

  public boolean removeJoinedProject(Project project) {
    return joinedProjects.remove(project);
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return isAccountNonExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return isAccountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return isCredentialsNotExpired;
  }

  @Override
  public boolean isEnabled() {
    return isEnabled;
  }

}
