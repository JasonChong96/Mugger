package com.bojio.mugger.authentication;

import java.util.HashMap;
import java.util.Map;

public class MuggerUser {
  private static MuggerUser user;
  private Map<String, Object> data;

  public MuggerUser() {
    this.data = new HashMap<>();
  }

  public MuggerUser getInstance() {
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
}
