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
  public static long WEEK_TO_MILLISECONDS = 1000 * 60 * 60 * 24 * 7;
  private static String[] DAYS_OF_THE_WEEK = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

  /**
   * Gets the timestamp of the same day as the input but at 00:00:00:000.
   * @param timestamp the input timestamp
   * @return timestamp of the same day as the input but the hour, minute, seconds and
   * milliseconds will be set to 0.
   */
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

  /**
   * Checks if 2 timestamps fall on the same date.
   * @param timestamp1 the first timestamp
   * @param timestamp2 the second timestamp
   * @return a boolean representing if the two input timestamps fall on the same date.
   */
  public static boolean isSameDate(long timestamp1, long timestamp2) {
    return getDayTimestamp(timestamp1) == getDayTimestamp(timestamp2);
  }

  /**
   * Checks how many days apart the two input timestamps are.
   * @param timestamp1 the first timestamp
   * @param timestamp2 the second timestamp
   * @return the number of day between the two timestamps.
   */
  public static long daysApart(long timestamp1, long timestamp2) {
    return ((ListingUtils.getDayTimestamp(timestamp1) - ListingUtils
        .getDayTimestamp(timestamp2)) / (1000 * 60 * 60 * 24)); // 1 week
  }

  /**
   * Get the user's modules available to use as filters from the input cache.
   * @param cache the cache to load modules from
   * @return an arraylist containing the module codes of the relevant modules
   */
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

  /**
   * Get the Firestore Query used to fetch all available listings, ordered by start time, from
   * the input Firestore database.
   * @param db the Firestore database
   * @return the Firestore query
   */
  public static Query getAvailableListingsQuery(FirebaseFirestore db) {
    return MuggerDatabase.getAllListingsReference(db)
        .orderBy("startTime");
  }

  /**
   * Get the Firestore Query used to fetch all listings that the user with the input unique id is
   * attending from the input Firestore database.
   * @param db the Firestore database
   * @param uid the unique id of the user
   * @return the Firestore query
   */
  public static Query getAttendingListingsQuery(FirebaseFirestore db, String uid) {
    return MuggerDatabase.getAllListingsReference(db)
        .orderBy(uid);
  }

  /**
   * Gets the Firestore query used to fetch all listings that is made by the user with the input
   * unique id from the input Firestore database.
   * @param db the Firestore database
   * @param uid the unique id of the user
   * @return the Firestore query
   */
  public static Query getMyListingsQuery(FirebaseFirestore db, String uid) {
    return MuggerDatabase.getAllListingsReference(db)
        .orderBy("startTime")
        .whereEqualTo("ownerId", uid);
  }

  /**
   * Checks if the input long is in between the input range. (exclusive)
   * @param check the long value to check
   * @param start the start of the range
   * @param end the end of the range
   * @return a boolean representing if the long value is within the range (exclusive)
   */
  public static boolean isBetween(long check, long start, long end) {
    return check > start && check < end;
  }

  /**
   * Gets the intuitive display for start and end date and time of listings.
   * @param startTime the start timestamp of the listing
   * @param endTime the end timestamp of the listing
   * @param df the DateFormat of the date
   * @param dfTime the DateFormat of the time
   * @return the String that represents the timing of the listings in an intuitive manner
   */
  public static String getStartEndTimeDisplay(long startTime, long endTime, DateFormat df,
                                              DateFormat dfTime) {
    Calendar startTimeDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"));
    Calendar endTimeDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"));
    startTimeDate.setTimeInMillis(startTime);
    endTimeDate.setTimeInMillis(endTime);
    // If the start and end time fall on the same date
    boolean sameDate = ListingUtils.isSameDate(startTime, endTime);
    // If the start time falls on today
    boolean startsToday = ListingUtils.isSameDate(System.currentTimeMillis(), startTime);
    // If the difference between today and the start date is less than 7 days apart (exclusive)
    boolean withinWeekCurToStart = Math.abs(ListingUtils.getDayTimestamp(startTime) - ListingUtils
        .getDayTimestamp(System.currentTimeMillis())) < WEEK_TO_MILLISECONDS; // 1 week
    // If the difference between today and the end date is less than 7 days apart (exclusive)
    boolean withinWeekCurToEnd = Math.abs(ListingUtils.getDayTimestamp(endTime) - ListingUtils
        .getDayTimestamp(System.currentTimeMillis())) < WEEK_TO_MILLISECONDS; // 1 week
    // If the difference between end date and the start date is less than 7 days apart (exclusive)
    boolean withinWeekStartToEnd = ListingUtils.getDayTimestamp(endTime) - ListingUtils
        .getDayTimestamp(startTime) < WEEK_TO_MILLISECONDS; // 1 week
    // If the end time is on the same date as today
    boolean endsToday = ListingUtils.isSameDate(System.currentTimeMillis(), endTime);
    // The number of days from today till the start day
    long daysTillStart = ListingUtils.daysApart(startTime, System.currentTimeMillis());
    // The number of days from today till the end day
    long daysTillEnd = ListingUtils.daysApart(endTime, System.currentTimeMillis());
    // The duration of the session in days
    long daysDuration = ListingUtils.daysApart(endTime, startTime);
    String startDateDisplay;
    String endDateDisplay;
    if (startsToday) {
      startDateDisplay = "Today";
    } else if (daysTillStart == 1) {
      startDateDisplay = "Tomorrow";
    } else if (daysTillStart == -1) {
      startDateDisplay = "Yesterday";
    } else if (withinWeekCurToStart) {
      startDateDisplay = (startTime < System.currentTimeMillis() ? "Last " : "This ") +
          DAYS_OF_THE_WEEK[startTimeDate.get(Calendar.DAY_OF_WEEK) - 1];
    } else {
      startDateDisplay = df.format(new Date(startTime)) + ", " +
          DAYS_OF_THE_WEEK[startTimeDate.get(Calendar.DAY_OF_WEEK) - 1];
    }
    if (sameDate) {
      endDateDisplay = "";
    } else if (endsToday) {
      endDateDisplay = "Today";
    } else if (daysTillEnd == 1) {
      endDateDisplay = "Tomorrow";
    } else if (daysDuration == 1) {
      endDateDisplay = "The day after,";
    } else if (withinWeekStartToEnd && withinWeekCurToStart) {
      endDateDisplay = DAYS_OF_THE_WEEK[endTimeDate.get(Calendar.DAY_OF_WEEK)];
    } else if (withinWeekCurToEnd) {
      endDateDisplay = (endTime < System.currentTimeMillis() ? "Last " : "This ") +
          DAYS_OF_THE_WEEK[endTimeDate.get(Calendar.DAY_OF_WEEK) - 1];
    } else {
      endDateDisplay = df.format(new Date(endTime)) + ", " + DAYS_OF_THE_WEEK[endTimeDate.get(Calendar
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

  /**
   * Gets the FirestoreRecyclerOptions for listings view with the input the Firesotre Query that
   * is synced with the life cycle of the input LifeCycleOwner.
   * @param mQuery the Firestore query
   * @param lifeCycleOwner the LifeCyclerOwner of the RecyclerView
   * @return FirestoreRecyclerOptions reference that encapsulates the query.
   */
  public static FirestoreRecyclerOptions<Listing> getRecyclerOptions(Query mQuery, LifecycleOwner
      lifeCycleOwner) {
    return new FirestoreRecyclerOptions.Builder<Listing>()
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

  /**
   * Get the date time display of the input Date object in the format of "(Date) (Time)" using
   * the given date and time DateFormats.
   * @param df the DateFormat for date
   * @param dfTime the DateFormat for time
   * @param time the Date object representing the time
   * @return String representing the date and time.
   */
  public static String getDateTimeDisplay(DateFormat df, DateFormat dfTime, Date time) {
    return new StringBuilder()
        .append(df.format(time))
        .append(" ")
        .append(dfTime.format(time))
        .toString();
  }
}
