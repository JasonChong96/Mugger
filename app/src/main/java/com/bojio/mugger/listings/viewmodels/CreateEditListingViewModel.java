package com.bojio.mugger.listings.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.fcm.MessagingService;
import com.bojio.mugger.listings.Listing;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CreateEditListingViewModel extends AndroidViewModel {
  private MuggerUserCache cache;
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private Listing toEdit;
  private DateFormat df;
  private DateFormat dfTime;
  private TreeMap<String, Byte> moduleRoles;
  private Calendar startDateTime;
  private Calendar endDateTime;
  private MutableLiveData<String> startTimeDateString;
  private MutableLiveData<String> endTimeDateString;
  private String moduleCode;

  public CreateEditListingViewModel(Application application) {
    super(application);
    cache = MuggerUserCache.getInstance();
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
    df = android.text.format.DateFormat.getDateFormat(application);
    dfTime = android.text.format.DateFormat.getTimeFormat(application);
    startDateTime = Calendar.getInstance();
    endDateTime = Calendar.getInstance();
    endDateTime.add(Calendar.HOUR_OF_DAY, 1);
    startTimeDateString = new MutableLiveData<>();
    endTimeDateString = new MutableLiveData<>();
  }

  public void init(Listing toEdit) {
    if (moduleRoles != null) {
      return;
    }
    moduleRoles = MuggerUserCache.getInstance().getModules().firstEntry().getValue();
    if (toEdit != null) {
      this.toEdit = toEdit;
      if (!moduleRoles.containsKey(toEdit.getModuleCode())) {
        moduleRoles.put(toEdit.getModuleCode(), (byte) toEdit.getType());
      }
      startDateTime.setTimeInMillis(toEdit.getStartTime());
      endDateTime.setTimeInMillis(toEdit.getEndTime());
      moduleCode = toEdit.getModuleCode();
    } else {
      moduleCode = moduleRoles.firstKey();
    }
    startTimeDateString.postValue(getDateTimeString(startDateTime));
    endTimeDateString.postValue(getDateTimeString(endDateTime));
  }

  public double getMutedTimeLeft() {
    return (double) cache.isMuted() / 3600000D;
  }

  public String getDateString(long time) {
    return df.format(time);
  }

  public MutableLiveData<String> getStartTimeDateString() {
    return startTimeDateString;
  }

  public MutableLiveData<String> getEndDateTimeString() {
    return endTimeDateString;
  }

  public String getModuleCode() {
    return moduleCode;
  }

  public void setModuleCode(String moduleCode) {
    this.moduleCode = moduleCode;
  }

  public String getOriginalVenue() {
    return toEdit.getVenue();
  }

  public String getOriginalDescription() {
    return toEdit.getDescription();
  }

  public ArrayList<String> getModuleSelections() {
    return new ArrayList<>(moduleRoles.keySet());
  }

  public void updateStartDate(int year, int month, int date) {
    startDateTime.set(year, month, date);
  }

  public void updateStartTime(int hour, int minute) {
    startDateTime.set(Calendar.HOUR_OF_DAY, hour);
    startDateTime.set(Calendar.MINUTE, minute);
    startTimeDateString.postValue(getDateTimeString(startDateTime));
  }

  public void updateEndDate(int year, int month, int date) {
    endDateTime.set(year, month, date);
  }

  public void updateEndTime(int hour, int minute) {
    endDateTime.set(Calendar.HOUR_OF_DAY, hour);
    endDateTime.set(Calendar.MINUTE, minute);
    endTimeDateString.postValue(getDateTimeString(endDateTime));
  }

  private String getDateTimeString(Calendar cal) {
    return new StringBuilder()
        .append(df.format(cal.getTime()))
        .append(" ")
        .append(dfTime.format(cal.getTime()))
        .toString();
  }

  public boolean is24HourFormat() {
    return android.text.format.DateFormat.is24HourFormat(getApplication().getApplicationContext());
  }

  public Calendar getStartDateTime() {
    return startDateTime;
  }

  public Calendar getEndDateTime() {
    return endDateTime;
  }

  public boolean startAfterEnd() {
    return startDateTime.after(endDateTime);
  }

  public boolean endBeforeNow() {
    return endDateTime.getTimeInMillis() < System.currentTimeMillis();
  }

  public Task<?> publish(String venue, String description) {
    Map<String, Object> data = new HashMap<>();
    long startTimeMillis = startDateTime.getTimeInMillis();
    data.put("description", description);
    data.put("endTime", endDateTime.getTimeInMillis());
    data.put("startTime", startTimeMillis);
    data.put("moduleCode", moduleCode);
    data.put("ownerId", toEdit == null ? mAuth.getCurrentUser().getUid() : toEdit.getOwnerId());
    data.put("ownerName", toEdit == null ? mAuth.getCurrentUser().getDisplayName() : toEdit
        .getOwnerName());
    data.put("venue", venue);
    if (toEdit == null) {
      data.put(mAuth.getUid(), startTimeMillis);
      Log.e("module Code", moduleCode);
      Log.e("module Roles", Byte.toString(moduleRoles.get(moduleCode)));
      data.put("type", (int) moduleRoles.get(moduleCode));
    }
    if (toEdit != null) {
      for (String attendee : toEdit.getAttendees()) {
        data.put(attendee, startTimeMillis);
      }
      data.put(toEdit.getModuleCode(), FieldValue.delete());
    }
    data.put(moduleCode, startTimeMillis);
    Task<?> addedDocRef;
    if (toEdit == null) {
      addedDocRef = MuggerDatabase.createListing(db, data);
    } else {
      addedDocRef = MuggerDatabase.getListingReference(db, toEdit.getUid()).set(data, SetOptions
          .merge());
    }
    addedDocRef.addOnSuccessListener(result -> {
      if (toEdit == null) {
        DocumentReference docRef = (DocumentReference) result;
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", "Listing Created");
        StringBuilder body = new StringBuilder();
        body.append(mAuth.getCurrentUser().getDisplayName()).append(" has created a Listing for ")
            .append(moduleCode)
            .append(".");
        notificationData.put("body", body.toString());
        notificationData.put("type", MessagingService.CREATED_NOTIFICATION);
        notificationData.put("fromUid", mAuth.getUid());
        notificationData.put("topicUid", moduleCode);
        notificationData.put("listingUid", docRef.getId());
        FirebaseMessaging.getInstance().subscribeToTopic(docRef.getId());
        MuggerDatabase.sendNotification(db, notificationData);
      }
    });
    return addedDocRef;
  }
}
