package com.bojio.mugger.listings.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;

import com.bojio.mugger.authentication.MuggerRole;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.fcm.MessagingService;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.ListingUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListingDetailsViewModel extends AndroidViewModel {
  private FirebaseAuth mAuth;
  private FirebaseFirestore db;
  private FirebaseMessaging fcm;
  private MuggerUserCache cache;
  private Listing listing;
  private long startTime;
  private long endTime;
  private MutableLiveData<String> startTimeString;
  private MutableLiveData<String> endTimeString;
  private MutableLiveData<String> venue;
  private MutableLiveData<String> description;
  private MutableLiveData<Boolean> deleted;
  private MutableLiveData<Integer> numAttendees;
  private MutableLiveData<Boolean> isAttending;
  private DateFormat df;
  private DateFormat dfTime;

  /**
   * Default constructor for this ViewModel.
   * @param app the application that this is contained in
   */
  public ListingDetailsViewModel(Application app) {
    super(app);
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    cache = MuggerUserCache.getInstance();
    df = android.text.format.DateFormat.getDateFormat(app);
    dfTime = android.text.format.DateFormat.getTimeFormat(app);
    startTimeString = new MutableLiveData<>();
    endTimeString = new MutableLiveData<>();
    venue = new MutableLiveData<>();
    deleted = new MutableLiveData<>();
    description = new MutableLiveData<>();
    numAttendees = new MutableLiveData<>();
    isAttending = new MutableLiveData<>();
    fcm = FirebaseMessaging.getInstance();
  }

  /**
   * Initializes this ViewModel with the given listingUid. The ViewModel will start listening for
   * changes in the data of this listing.
   * @param listingUid
   */
  public void init(String listingUid) {
    if (listing != null) {
      return;
    }
    MuggerDatabase.getListingReference(db, listingUid).addSnapshotListener((snapshot, e) -> {
      if (snapshot.exists()) {
        // Module code is not included here as it cannot be edited once the listing has been made.
        listing = Listing.getListingFromSnapshot(snapshot);
        startTime = updateTime(snapshot, "startTime", startTime, startTimeString);
        endTime = updateTime(snapshot, "endTime", endTime, endTimeString);
        updateString(snapshot, "venue", venue);
        if (numAttendees.getValue() == null ||
            listing.getNumAttendees() != numAttendees.getValue()) {
          numAttendees.setValue(listing.getNumAttendees());
          isAttending.setValue(listing.isAttending(mAuth.getUid()));
        }
        updateString(snapshot, "description", description);
      } else {
        deleted.setValue(true);
      }
    });
  }

  private long updateTime(DocumentSnapshot snapshot, String fieldName, long oldValue,
                          MutableLiveData<String> liveData) {
    Long newValue = snapshot.getLong(fieldName);
    if (newValue != null && !newValue.equals(oldValue)) {
      Date newDate = new Date(newValue);
      liveData.setValue(ListingUtils.getDateTimeDisplay(df, dfTime, newDate));
      return newValue;
    }
    return oldValue;
  }

  private void updateString(DocumentSnapshot snapshot, String fieldName,
                              MutableLiveData<String> liveData) {
    String newValue = snapshot.getString(fieldName);
    if (newValue != null && !newValue.equals(liveData.getValue())) {
      liveData.setValue(newValue);
    }
  }

  public MutableLiveData<String> getStartTimeString() {
    return startTimeString;
  }

  public MutableLiveData<String> getEndTimeString() {
    return endTimeString;
  }

  public MutableLiveData<String> getVenue() {
    return venue;
  }

  public MutableLiveData<String> getDescription() {
    return description;
  }

  public MutableLiveData<Boolean> getDeleted() {
    return deleted;
  }

  public DateFormat getDf() {
    return df;
  }

  public String getModuleCode() {
    return listing.getModuleCode();
  }

  public MutableLiveData<Integer> getNumAttendees() {
    return numAttendees;
  }

  public MutableLiveData<Boolean> getIsAttending() {
    return isAttending;
  }

  public boolean isOwnListing() {
    return mAuth.getUid().equals(listing.getOwnerId());
  }

  public boolean listingFull() {
    return listing.getNumAttendees() > 19;
  }

  public void joinListing() {
    fcm.subscribeToTopic(listing.getUid());
    MuggerDatabase.getListingReference(db, listing.getUid())
        .update(mAuth.getUid(), listing.getStartTime());
  }

  public void unjoinListing() {
    fcm.unsubscribeFromTopic(listing.getUid());
    MuggerDatabase.getListingReference(db, listing.getUid())
        .update(mAuth.getUid(), FieldValue.delete());
  }

  public Listing getListing() {
    return listing;
  }

  public boolean canEditDelete() {
    if (listing == null) {
      return false;
    } else {
      return isOwnListing() || MuggerRole.MODERATOR.check(cache.getRole());
    }
  }

  public Task<Void> deleteListing() {
    Task<Void> task = MuggerDatabase.deleteListing(db, listing.getUid());
    task.addOnSuccessListener(taskk -> {
      Map<String, Object> notificationData = new HashMap<>();
      notificationData.put("title", "Listing Deleted");
      StringBuilder body = new StringBuilder();
      body.append(listing.getOwnerName()).append("'s ").append(listing.getModuleCode())
          .append(" Listing has been deleted.");
      notificationData.put("body", body.toString());
      notificationData.put("type", MessagingService.DELETED_NOTIFICATION);
      notificationData.put("fromUid", mAuth.getUid());
      notificationData.put("topicUid", listing.getUid());
      MuggerDatabase.sendNotification(db, notificationData);
    });
    return task;
  }

  public String getListingUid() {
    return listing.getUid();
  }

  public List<String> getAttendees() {
    return listing.getAttendees();
  }

  public String getOwnerUid() {
    return listing.getOwnerId();
  }
}
