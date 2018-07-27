package com.bojio.mugger.listings.viewmodels;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;

import com.annimon.stream.function.Predicate;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.constants.ModuleRole;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.listings.ListingUtils;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import es.dmoral.toasty.Toasty;

public class CustomFilterListingsViewModel extends ListingsFragmentsViewModel {
  public static final int MASK_CATEGORY = 0x3;
  public static final int FLAG_ALL_LISTINGS = 0x0;
  public static final int FLAG_JOINING_LISTINGS = 0x1;
  public static final int FLAG_MY_LISTINGS = 0x2;
  public static final int FLAG_STUDENT = 0x4;
  public static final int FLAG_TEACHING_ASSISTANT = 0x8;
  public static final int FLAG_PROFESSOR = 0x10;
  private MuggerUserCache cache;
  private MutableLiveData<Boolean> showUnrelatedModules;
  private ListenerRegistration listener;
  private java.text.DateFormat df;

  public CustomFilterListingsViewModel(@NonNull Application application) {
    super(application, null);
    this.cache = MuggerUserCache.getInstance();
    this.showUnrelatedModules = new MutableLiveData<>();
    df = DateFormat.getDateFormat(application.getApplicationContext());
    init();
  }

  private static <T> Predicate<T> composePredicates(Predicate<T> predicate1, Predicate<T>
      predicate2) {
    return x -> predicate1.test(x) && predicate2.test(x);
  }

  public void init() {
    super.init();
    listener =
        MuggerDatabase.getUserReference(db, mAuth.getUid()).addSnapshotListener((documentSnapshot, e) -> {
          Long newValue = documentSnapshot.getLong("showUnrelatedModules");
          Boolean newValueBoolean = newValue != null && newValue != 0;
          if (!newValueBoolean.equals(showUnrelatedModules.getValue())) {
            showUnrelatedModules.postValue(newValueBoolean);
          }
        });
  }

  public ArrayList<String> getModuleFilters() {
    return ListingUtils.getFilterModules(cache);
  }

  public MutableLiveData<Boolean> getShowUnrelatedModules() {
    return showUnrelatedModules;
  }

  public boolean hasPreviouslySetFlags() {
    return cache.getData().containsKey("customFilterSettings");
  }

  public int getCustomFilterFlags() {
    return ((Long) cache.getData().get("customFilterSettings")).intValue();
  }

  public String getFilterModule() {
    return (String) cache.getData().get("customFilterModule");
  }

  public String getFilterCreator() {
    return (String) cache.getData().get("customFilterCreator");
  }

  public String getFilterVenue() {
    return (String) cache.getData().get("customFilterVenue");
  }

  public String getFilterDesc() {
    return (String) cache.getData().get("customFilterDesc");
  }

  public long getFilterFromDate() {
    Long from = (Long) cache.getData().get("customFilterFromDate");
    long cur = ListingUtils.getDayTimestamp(System.currentTimeMillis());
    if (from == null || from < cur) {
      return cur;
    }
    return from;
  }

  public String getStringFilterFromDate() {
    return df.format(new Date(getFilterFromDate()));
  }

  public String getDateString(int year, int month, int day) {
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"));
    c.set(year, month, day);
    return df.format(new Date(c.getTimeInMillis()));
  }

  public long getFilterToDate() {
    Long from = (Long) cache.getData().get("customFilterToDate");
    return from == null ? ListingUtils.DEFAULT_TIME_FILTER_END : from;
  }

  public String getStringFilterToDate() {
    return df.format(new Date(getFilterToDate()));
  }

  public void updateFilter(int flag, String moduleFilter, String creatorFilter, String
      venueFilter, String descFilter, String fromDateFilter, String toDateFilter,
                           boolean changed) {
    Map<String, Object> data = new HashMap<>();
    predicate = x -> true;
    switch (flag & MASK_CATEGORY) {
      case CustomFilterListingsViewModel.FLAG_ALL_LISTINGS:
        mQuery = ListingUtils.getAvailableListingsQuery(db);
        break;
      case CustomFilterListingsViewModel.FLAG_JOINING_LISTINGS:
        mQuery = ListingUtils.getAttendingListingsQuery(db, mAuth.getUid());
        break;
      case CustomFilterListingsViewModel.FLAG_MY_LISTINGS:
        mQuery = ListingUtils.getMyListingsQuery(db, mAuth.getUid());
        break;
      default:
        throw new IllegalStateException("No valid category selected");
    }
    if (!moduleFilter.isEmpty()) {
      predicate = composePredicates(predicate, listing -> listing.getModuleCode()
          .equals(moduleFilter));
      data.put("customFilterModule", moduleFilter);
    } else {
      data.put("customFilterModule", FieldValue.delete());
    }
    if ((flag & FLAG_STUDENT) == 0) {
      predicate = composePredicates(predicate, listing -> listing.getType() !=
          ModuleRole.EMPTY);
    }
    if ((flag & FLAG_TEACHING_ASSISTANT) == 0) {
      predicate = composePredicates(predicate, listing -> listing.getType() !=
          ModuleRole.TEACHING_ASSISTANT);
    }
    if ((flag & FLAG_PROFESSOR) == 0) {
      predicate = composePredicates(predicate, listing -> listing.getType() !=
          ModuleRole.PROFESSOR);
    }
    if (!creatorFilter.isEmpty()) {
      predicate = composePredicates(predicate, listing -> listing.getOwnerName()
          .contains(creatorFilter));
      data.put("customFilterCreator", creatorFilter);
    } else {
      data.put("customFilterCreator", FieldValue.delete());
    }
    if (!venueFilter.isEmpty()) {
      predicate = composePredicates(predicate, listing -> listing.getVenue()
          .contains(venueFilter));
      data.put("customFilterVenue", venueFilter);
    } else {
      data.put("customFilterVenue", FieldValue.delete());
    }
    if (!descFilter.isEmpty()) {
      predicate = composePredicates(predicate, listing -> listing.getDescription()
          .contains(descFilter));
      data.put("customFilterDesc", descFilter);
    } else {
      data.put("customFilterDesc", FieldValue.delete());
    }
    try {
      long fromDate = df.parse(fromDateFilter).getTime();
      long toDate = df.parse(toDateFilter).getTime();
      if (toDate >= fromDate) {
        predicate = composePredicates(predicate, listing -> {
          if (listing.getStartTime() < fromDate && listing.getEndTime() > fromDate) {
            return true;
          } else if (ListingUtils.isBetween(listing.getStartTime(), fromDate - 1, toDate + 24 *
              60 * 60 * 1000)) {
            return true;
          } else {
            return false;
          }
        });
        data.put("customFilterFromDate", fromDate);
        data.put("customFilterToDate", toDate);
      } else {
        Toasty.warning(getApplication().getApplicationContext(), "Date filters were not applied " +
            "as the chosen end date is earlier than the start date.").show();
      }
    } catch (ParseException e) {
      Toasty.error(getApplication().getApplicationContext(), "Error including date filters, " +
          "please try to re-input the dates.").show();
    }
    data.put("customFilterSettings", Long.valueOf(flag));
    if (changed) {
      MuggerDatabase.getUserReference(db, mAuth.getUid()).update(data);
      cache.updateCache(data);
    }
  }

  @Override
  public void onCleared() {
    listener.remove();
  }
}
