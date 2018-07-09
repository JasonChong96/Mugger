package com.bojio.mugger.listings;

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

  public static int daysApart(long timestamp1, long timestamp2) {
    return (int) ((ListingUtils.getDayTimestamp(timestamp1) - ListingUtils
        .getDayTimestamp(timestamp2)) / (1000 * 60 * 60 * 24)); // 1 week
  }
}
