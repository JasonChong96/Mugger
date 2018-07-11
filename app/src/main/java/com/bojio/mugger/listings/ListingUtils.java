package com.bojio.mugger.listings;

import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class ListingUtils {
  public static long DEFAULT_TIME_FILTER_END = 4102415999000L;

  public static long getDayTimestamp(long timestamp) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
    calendar.setTimeInMillis(timestamp);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MINUTE, 0);
    return calendar.getTimeInMillis();
  }

  public static boolean isSameDate(long timestamp1, long timestamp2) {
    return getDayTimestamp(timestamp1) == getDayTimestamp(timestamp2);
  }

  public static long daysApart(long timestamp1, long timestamp2) {
    return ((ListingUtils.getDayTimestamp(timestamp1) - ListingUtils
        .getDayTimestamp(timestamp2)) / (1000 * 60 * 60 * 24)); // 1 week
  }

  public static ArrayList<String> getFilterModules(MuggerUserCache cache) {
    ArrayList<String> modules;
    long unrelatedModules = 0L;
    if (cache.getData().containsKey("showUnrelatedModules")) {
      unrelatedModules = (long) cache.getData().get("showUnrelatedModules");
    }
    if (unrelatedModules != 0) {
      modules = new ArrayList<>(cache.getAllModules());;
    } else {
      modules = new ArrayList<>(cache.getModules().firstEntry()
          .getValue().keySet());
    }
    modules.add(0, "Show all modules");
    return modules;
  }

  public static Query getAvailableListingsQuery(FirebaseFirestore db) {
    return MuggerDatabase.getAllListingsReference(db)
        .orderBy("startTime");
  }

  public static Query getAttendingListingsQuery(FirebaseFirestore db, String uid) {
    return MuggerDatabase.getAllListingsReference(db)
        .orderBy(uid);
  }

  public static Query getMyListingsQuery(FirebaseFirestore db, String uid) {
    return MuggerDatabase.getAllListingsReference(db)
        .orderBy("startTime")
        .whereEqualTo("ownerId", uid);
  }

}
