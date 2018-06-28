package com.bojio.mugger.authentication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MuggerUser {
  private static MuggerUser user;
  private MuggerRole role;
  private TreeMap<String, TreeMap<String, Byte>> modules;
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

  public long isMuted() {
    Long mutedTill = (Long) data.get("muted");
    if (mutedTill == null) {
      return 0;
    } else if (mutedTill < System.currentTimeMillis()) {
      data.remove("muted");
      FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance()
          .getUid()).update("muted", FieldValue.delete());
      return 0;
    } else {
      return mutedTill - System.currentTimeMillis();
    }
  }

  public Map<String, Object> getData() {
    return data;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
    role = MuggerRole.getByRoleId((Long) data.get("roleId"));
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
