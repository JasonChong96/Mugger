package com.bojio.mugger.constants;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Modules {
  private static Set<String> irrelevantCodes;
  private static List<String> irrelevantNames;

  public static boolean isRelevantModule(String code, String name) {
    if (irrelevantCodes == null) {
      irrelevantCodes = new HashSet<>();
    }
    if (irrelevantNames == null) {
      irrelevantNames = new LinkedList<>();
    }
    if (irrelevantCodes.isEmpty()) {
      irrelevantCodes.add("DP1001");
    }
    if (irrelevantNames.isEmpty()) {
      irrelevantNames.add("Advanced Placement");
      irrelevantNames.add("For Poly Graduates");
    }
    if (irrelevantCodes.contains(code)) {
      return false;
    } else {
      for (String toCheckAgainst : irrelevantNames) {
        if (name.contains(toCheckAgainst)) {
          return false;
        }
      }
      return true;
    }
  }
}
