package com.bojio.mugger.listings;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Listing implements Parcelable {
  public static final Parcelable.Creator<Listing> CREATOR
      = new Parcelable.Creator<Listing>() {
    public Listing createFromParcel(Parcel in) {
      return new Listing(in);
    }

    public Listing[] newArray(int size) {
      return new Listing[size];
    }
  };
  private String uid;
  private String moduleCode;
  private long startTime;
  private long endTime;
  private String description;
  private String ownerId;
  private String venue;


  private String ownerName;


  private int type;


  private List<String> attendees;

  public Listing(String uid, String ownerName, String ownerId, String moduleCode, long startTime,
                  long endTime, String description, String venue, List<String> attendees, int type) {
    this.uid = uid;
    this.ownerName = ownerName;
    this.ownerId = ownerId;
    this.moduleCode = moduleCode;
    this.startTime = startTime;
    this.endTime = endTime;
    this.description = description;
    this.venue = venue;
    this.attendees = attendees;
    this.type = type;
  }

  private Listing(Parcel source) {
    this.uid = source.readString();
    this.ownerName = source.readString();
    this.ownerId = source.readString();
    this.moduleCode = source.readString();
    this.description = source.readString();
    this.startTime = source.readLong();
    this.endTime = source.readLong();
    this.venue = source.readString();
    this.attendees = new ArrayList<String>();
    source.readStringList(this.attendees);
    this.type = source.readInt();
  }

  public static Listing getListingFromSnapshot(DocumentSnapshot snapshot) {
    if (snapshot == null || !snapshot.exists() || snapshot.getData() == null) {
      return null;
    }
    Map<String, Object> data = snapshot.getData();
    int type = 0;
    if (snapshot.contains("type")) {
      type = ((Long) snapshot.get("type")).intValue();
    }
    data.remove("ownerId");
    data.remove("moduleCode");
    data.remove("startTime");
    data.remove("endTime");
    data.remove("description");
    data.remove("venue");
    data.remove("type");
    data.remove("ownerName");
    data.remove(snapshot.get("moduleCode"));
    List<String> attendeesList = new ArrayList<>(data.keySet());

    return new Listing(snapshot.getId(),
        (String) snapshot.get("ownerName"),
        (String) snapshot.get("ownerId"),
        (String) snapshot.get("moduleCode"),
        (long) snapshot.get("startTime"),
        (long) snapshot.get("endTime"),
        (String) snapshot.get("description"),
        (String) snapshot.get("venue"),
        attendeesList,
        type
    );
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(uid);
    dest.writeString(ownerName);
    dest.writeString(ownerId);
    dest.writeString(moduleCode);
    dest.writeString(description);
    dest.writeLong(startTime);
    dest.writeLong(endTime);
    dest.writeString(venue);
    dest.writeStringList(this.attendees);
    dest.writeInt(type);
  }

  public String getVenue() {
    return venue;
  }

  public void setVenue(String venue) {
    this.venue = venue;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public String getModuleCode() {
    return moduleCode;
  }

  public void setModuleCode(String moduleCode) {
    this.moduleCode = moduleCode;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public List<String> getAttendees() {
    return attendees;
  }

  public int getNumAttendees() {
    return attendees.size();
  }

  public boolean isAttending(String uid) {
    return attendees.contains(uid);
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof Listing) {
      return this.getUid().equals(((Listing) other).getUid());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return this.getUid().hashCode();
  }
}
