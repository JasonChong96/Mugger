package com.bojio.mugger.authentication;

import com.bojio.mugger.constants.ModuleRole;
import com.bojio.mugger.constants.MuggerRole;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MuggerUser {
  private static MuggerUser user;
  private MuggerRole role;
  private TreeMap<String, TreeMap<String, Byte>> modules;

  public void setData(Map<String, Object> data) {
    this.data = data;
    role = MuggerRole.getByRoleId((Long) data.get("roleId"));
  }

  private Map<String, Object> data;

  public MuggerUser() {
    this.data = new HashMap<>();
  }

  public static MuggerUser getInstance() {
    if (user == null) {
      user = new MuggerUser();
    }
    return user;
  }

  public static void clear() {
    user = new MuggerUser();
  }

  public Map<String, Object> getData() {
    return data;
  }

  public MuggerRole getRole() {
    return role;
  }

  public void setRole(MuggerRole role) {
    this.role = role;
  }

  public TreeMap<String, TreeMap<String, Byte>> getModules() {
    return modules;
  }

  public void setModules(TreeMap<String, TreeMap<String, Byte>> modules) {
    this.modules = modules;
  }
}
