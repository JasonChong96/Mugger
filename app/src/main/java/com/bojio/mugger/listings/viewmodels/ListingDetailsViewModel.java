package com.bojio.mugger.listings.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.widget.Toast;

import com.bojio.mugger.authentication.MuggerRole;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.fcm.MessagingService;
import com.bojio.mugger.listings.Listing;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

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

  public void init(String listingUid) {
    if (listing != null) {
      return;
    }
    MuggerDatabase.getListingReference(db, listingUid).addSnapshotListener((snapshot, e) -> {
      if (snapshot.exists()) {
        listing = Listing.getListingFromSnapshot(snapshot);
        Long newStart = snapshot.getLong("startTime");
        if (newStart != null && !newStart.equals(startTime)) {
          Date start = new Date(newStart);
          startTimeString.setValue(new StringBuilder()
              .append(df.format(start))
              .append(" ")
              .append(dfTime.format(start))
              .toString());
          startTime = newStart;
        }
        Long newEnd = snapshot.getLong("endTime");
        if (newEnd != null && !newEnd.equals(endTime)) {
          Date end = new Date(endTime);
          endTimeString.setValue(new StringBuilder()
              .append(df.format(end))
              .append(" ")
              .append(dfTime.format(end))
              .toString());
          endTime = newEnd;
        }
        String newVenue = snapshot.getString("venue");
        if (newVenue != null && !newVenue.equals(venue.getValue())) {
          venue.setValue(newVenue);
        }
        if (numAttendees.getValue() == null ||
            listing.getNumAttendees() != numAttendees.getValue()) {
          numAttendees.setValue(listing.getNumAttendees());
          isAttending.setValue(listing.isAttending(mAuth.getUid()));
        }
        String newDesc = snapshot.getString("description");
        if (newDesc != null && !newDesc.equals(description.getValue())) {
          description.setValue(newDesc);
        }
      } else {
        deleted.setValue(true);
      }
    });
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
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
