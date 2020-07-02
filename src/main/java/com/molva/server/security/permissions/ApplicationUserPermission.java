package com.molva.server.security.permissions;

public enum ApplicationUserPermission {
  MODERATOR_WRITE("moderator:write"),
  ADMIN_WRITE("admin:write");

  private final String permission;

  ApplicationUserPermission(String permission) {
    this.permission = permission;
  }

  public String getPermission() {
    return permission;
  }
}