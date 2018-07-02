package com.bojio.mugger.authentication;

import com.bojio.mugger.constants.ModuleRole;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MuggerUserCache {
  private static MuggerUserCache user;
  private MuggerRole role;
  private TreeMap<String, TreeMap<String, Byte>> modules;
  private Map<String, Object> data;

  public MuggerUserCache() {
    this.data = new HashMap<>();
  }

  public static MuggerUserCache getInstance() {
    if (user == null) {
      user = new MuggerUserCache();
    }
    return user;
  }

  public static void clear() {
    user = new MuggerUserCache();
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

  public void loadModules(List<DocumentSnapshot> docs) {
    TreeMap<String, TreeMap<String, Byte>> modules = new TreeMap<>(Collections.reverseOrder());
    for (DocumentSnapshot doc : docs) {
      TreeMap<String, Byte> mods = new TreeMap<>();
      modules.put(doc.getId().replace(".", "/"), mods);
      for (String mod : (List<String>) doc.get("moduleCodes")) {
        mods.put(mod, ModuleRole.EMPTY);
      }
      List<String> ta = (List<String>) doc.get("ta");
      if (ta != null) {
        for (String mod : ta) {
          mods.put(mod, ModuleRole.TEACHING_ASSISTANT);
        }
      }
      List<String> prof = (List<String>) doc.get("professor");
      if (prof != null) {
        for (String mod : (List<String>) doc.get("professor")) {
          mods.put(mod, ModuleRole.PROFESSOR);
        }
      }
    }
    setModules(modules);
  }
}