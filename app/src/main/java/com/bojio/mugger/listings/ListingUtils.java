package com.bojio.mugger.listings;

import android.arch.lifecycle.LifecycleOwner;

import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
      modules = new ArrayList<>(cache.getAllModules());
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

  public static boolean isBetween(long check, long start, long end) {
    return check > start && check < end;
  }

  public static String getStartEndTimeDisplay(long startTime, long endTime, DateFormat df,
                                              DateFormat dfTime) {
    Calendar startTimeDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"));
    Calendar endTimeDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"));
    startTimeDate.setTimeInMillis(startTime);
    endTimeDate.setTimeInMillis(endTime);
    boolean sameDate = ListingUtils.isSameDate(startTime, endTime);
    boolean isToday = ListingUtils.isSameDate(System.currentTimeMillis(), startTime);
    boolean withinWeekCurToStart = Math.abs(ListingUtils.getDayTimestamp(startTime) - ListingUtils
        .getDayTimestamp(System.currentTimeMillis())) < 1000 * 60 * 60 * 24 * 7; // 1 week
    boolean withinWeekCurToEnd = Math.abs(ListingUtils.getDayTimestamp(endTime) - ListingUtils
        .getDayTimestamp(System.currentTimeMillis())) < 1000 * 60 * 60 * 24 * 7; // 1 week
    boolean withinWeekStartToEnd = ListingUtils.getDayTimestamp(endTime) - ListingUtils
        .getDayTimestamp(startTime) < 1000 * 60 * 60 * 24 * 7; // 1 week
    boolean endsToday = ListingUtils.isSameDate(System.currentTimeMillis(), endTime);
    String startDateDisplay;
    long daysTillStart = ListingUtils.daysApart(startTime, System.currentTimeMillis());
    long daysTillEnd = ListingUtils.daysApart(endTime, System.currentTimeMillis());
    long daysDuration = ListingUtils.daysApart(endTime, startTime);
    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    if (isToday) {
      startDateDisplay = "Today";
    } else if (daysTillStart == 1) {
      startDateDisplay = "Tomorrow";
    } else if (daysTillStart == -1) {
      startDateDisplay = "Yesterday";
    } else if (withinWeekCurToStart) {
      startDateDisplay = (startTime < System.currentTimeMillis() ? "Last " : "This ") +
          days[startTimeDate.get(Calendar.DAY_OF_WEEK) - 1];
    } else {
      startDateDisplay = df.format(new Date(startTime)) + ", " +
          days[startTimeDate.get(Calendar.DAY_OF_WEEK) - 1];
    }
    String endDateDisplay;

    if (sameDate) {
      endDateDisplay = "";
    } else if (endsToday) {
      endDateDisplay = "Today";
    } else if (daysTillEnd == 1) {
      endDateDisplay = "Tomorrow";
    } else if (daysDuration == 1) {
      endDateDisplay = "The day after,";
    } else if (withinWeekStartToEnd && withinWeekCurToStart) {
      endDateDisplay = days[endTimeDate.get(Calendar.DAY_OF_WEEK)];
    } else if (withinWeekCurToEnd) {
      endDateDisplay = (endTime < System.currentTimeMillis() ? "Last " : "This ") +
          days[endTimeDate.get(Calendar.DAY_OF_WEEK) - 1];
    } else {
      endDateDisplay = df.format(new Date(endTime)) + ", " + days[endTimeDate.get(Calendar
          .DAY_OF_WEEK) - 1];
    }
    String startTimeDisplay = dfTime.format(new Date(startTime));
    String endTimeDisplay = dfTime.format(new Date(endTime));
    StringBuilder sb = new StringBuilder(startDateDisplay);
    return sb.append(" ")
        .append(startTimeDisplay)
        .append(" to \n")
        .append(endDateDisplay)
        .append(endDateDisplay.isEmpty() ? "" : " ")
        .append(endTimeDisplay)
        .toString();
  }

  public static FirestoreRecyclerOptions<Listing> getRecyclerOptions(Query mQuery, LifecycleOwner
      lifeCycleOwner) {
    return new FirestoreRecyclerOptions
        .Builder<Listing>()
        .setQuery(mQuery, snapshot -> {
          if ((Long) snapshot.get("endTime") < System.currentTimeMillis()) {
            // Delete outdated entries
            snapshot.getReference().delete();
          }
          return Listing.getListingFromSnapshot(snapshot);
        })
        .setLifecycleOwner(lifeCycleOwner)
        .build();
  }

  public static String getDateTimeDisplay(DateFormat df, DateFormat dfTime, Date time) {
    return new StringBuilder()
        .append(df.format(time))
        .append(" ")
        .append(dfTime.format(time))
        .toString();
  }
}
