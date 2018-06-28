package com.bojio.mugger.authentication;

public enum MuggerRole {

  USER(0, true),
  MODERATOR(1, false),
  ADMIN(2, true),
  MASTER(3, true);

  private int roleId;
  private boolean enabled;

  MuggerRole(int roleId, boolean enabled) {
    this.roleId = roleId;
    this.enabled = enabled;
  }

  public static MuggerRole getByRoleId(int roleId) {
    for (MuggerRole role : values()) {
      if (role.roleId == roleId) {
        return role;
      }
    }
    return USER;
  }

  public static MuggerRole getByRoleId(Long roleId) {
    if (roleId == null) {
      return USER;
    }
    int roleIdd = roleId.intValue();
    for (MuggerRole role : values()) {
      if (role.roleId == roleIdd) {
        return role;
      }
    }
    return USER;
  }

  public int getRoleId() {
    return roleId;
  }

  public boolean check(MuggerRole toCheck) {
    return toCheck.roleId >= roleId;
  }

  public boolean checkSuperiorityTo(MuggerRole toCheck) {
    return roleId > toCheck.roleId;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
