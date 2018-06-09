package com.bojio.mugger.constants;

public enum MuggerRole {

  USER(0),
  MODERATOR(1),
  ADMIN(2),
  MASTER(3);

  private int roleId;

  MuggerRole(int roleId) {
    this.roleId = roleId;
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
}
