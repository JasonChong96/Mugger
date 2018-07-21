package com.bojio.mugger.listings.viewmodels;

import android.arch.lifecycle.ViewModel;

import com.annimon.stream.function.Predicate;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.ListingUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

/**
 * The ViewModel for MyScheduleFragments used in the My Schedule feature. Loads appropriate data
 * from the model for the UI.
 */
public class MyScheduleViewModel extends ViewModel {
  /**
   * Firestore database API instance.
   **/
  private FirebaseFirestore db;

  /**
   * Firebase Authentication API instance.
   **/
  private FirebaseAuth mAuth;

  /**
   * The currently selected day
   **/
  private CalendarDay selectedDay;

  /**
   * Collection of days that are currently marked
   **/
  private Collection<CalendarDay> markedDays;

  /**
   * Query used to fetch relevant listings
   **/
  private Query mQuery;

  /**
   * Constructor for my schedule view model. Initializes references to Firestore database and
   * Firebase Authentication.
   */
  public MyScheduleViewModel() {
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
    mQuery = ListingUtils.getAttendingListingsQuery(db, mAuth.getUid());
  }

  /**
   * Loads and returns the days that are to be marked, i.e days in which the user is attending a
   * study session. Compulsory to un this on a background thread as this thread will have to wait
   * for the Firestore API to return data.
   *
   * @param refresh fetches data from Firestore if refresh is true, else returns data from cache.
   * @return A Collection containing the CalendarDays that are to be marked
   */
  public Collection<CalendarDay> getMarkedDays(boolean refresh) {
    if (refresh || markedDays == null) {
      markedDays = new HashSet<>();
      Task<QuerySnapshot> task = ListingUtils.getAttendingListingsQuery(db, mAuth.getUid()).get();
      try {
        Tasks.await(task);
      } catch (ExecutionException | InterruptedException e) {
        return null;
      }
      if (task.isSuccessful()) {
        for (DocumentSnapshot docSnap : task.getResult().getDocuments()) {
          long start = (Long) docSnap.get("startTime");
          long end = (Long) docSnap.get("endTime");
          Date curDate = new Date(ListingUtils.getDayTimestamp(start));
          while (curDate.getTime() < end) {
            markedDays.add(CalendarDay.from(curDate));
            curDate = new Date(curDate.getTime() + 24 * 60 * 60 * 1000);
          }
        }
      }
    }
    return markedDays;
  }

  public CalendarDay getSelectedDay() {
    return selectedDay;
  }

  public void setSelectedDay(CalendarDay selectedDay) {
    this.selectedDay = selectedDay;
  }

  /**
   * Get the filter used to determine which listings to show to the user on the UI, i.e those
   * that are relevant to the day selected.
   *
   * @param day the day selected by the user
   * @return A predicate that returns true if the listing is to be shown, false if not.
   */
  public Predicate<Listing> getFilter(CalendarDay day) {
    long dayTimeStamp = day.getDate().getTime();
    long nextDayTimeStamp = dayTimeStamp + 24 * 60 * 60 * 1000;
    return listing -> {
      if (ListingUtils.isBetween(listing.getStartTime() - 1, dayTimeStamp, nextDayTimeStamp)) {
        return true;
      } else if (listing.getStartTime() < dayTimeStamp && listing.getEndTime() >= dayTimeStamp) {
        return true;
      } else {
        return false;
      }
    };
  }

  public Query getQuery() {
    return mQuery;
  }
}
