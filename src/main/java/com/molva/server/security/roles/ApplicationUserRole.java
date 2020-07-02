package com.molva.server.security.roles;

import com.google.common.collect.Sets;
import com.molva.server.security.permissions.ApplicationUserPermission;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static com.molva.server.security.permissions.ApplicationUserPermission.ADMIN_WRITE;
import static com.molva.server.security.permissions.ApplicationUserPermission.MODERATOR_WRITE;

public enum ApplicationUserRole {
  ADMIN(Sets.newHashSet(
      ADMIN_WRITE
  )),
  MODERATOR(Sets.newHashSet(
      MODERATOR_WRITE
  ));

  private final Set<ApplicationUserPermission> permissions;

  ApplicationUserRole(Set<ApplicationUserPermission> permissions) {
    this.permissions = permissions;
  }

  public Set<ApplicationUserPermission> getPermissions() {
    return permissions;
  }

  public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
    Set<SimpleGrantedAuthority> permissions = getPermissions().stream()
        .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
        .collect(Collectors.toSet());
    permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
    return permissions;
  }
}
