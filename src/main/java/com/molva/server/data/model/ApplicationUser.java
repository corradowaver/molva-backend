package com.molva.server.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.molva.server.security.roles.ApplicationUserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Entity
@Table(name = "application_user")
public class ApplicationUser implements UserDetails {
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

  @OneToOne(cascade = CascadeType.REMOVE)
  @JsonIgnore
  @JoinColumn(name = "profile")
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
  private Set<Project> projects;

  public ApplicationUser() {

  }

  public ApplicationUser(String username,
                         String password,
                         String email,
                         ApplicationUserRole applicationUserRole,
                         Set<Project> projects,
                         boolean isAccountNonExpired,
                         boolean isAccountNonLocked,
                         boolean isCredentialsNotExpired,
                         boolean isEnabled) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.applicationUserRole = applicationUserRole;
    this.projects = projects;
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

  public Set<Project> getProjects() {
    return projects;
  }

  public void setProjects(Set<Project> projects) {
    this.projects = projects;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  public Profile getProfile() {
    return profile;
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

  public void setId(Long id) {
    this.id = id;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setApplicationUserRole(ApplicationUserRole applicationUserRole) {
    this.applicationUserRole = applicationUserRole;
  }

  public void setAccountNonExpired(boolean accountNonExpired) {
    isAccountNonExpired = accountNonExpired;
  }

  public void setAccountNonLocked(boolean accountNonLocked) {
    isAccountNonLocked = accountNonLocked;
  }

  public void setCredentialsNotExpired(boolean credentialsNotExpired) {
    isCredentialsNotExpired = credentialsNotExpired;
  }

  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }

  public void setEmail(String email) {
    this.email = email;
  }


  public void setProfile(Profile profile) {
    this.profile = profile;
  }
}
