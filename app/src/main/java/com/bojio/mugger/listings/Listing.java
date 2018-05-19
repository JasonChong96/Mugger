package com.bojio.mugger.listings;

import android.os.Parcel;
import android.os.Parcelable;

public class Listing implements Parcelable {
    private String moduleCode;
    private long startTime;
    private long endTime;
    private String description;
    private String ownerId;
    private String venue;

    public Listing(String ownerId, String moduleCode, long startTime, long endTime, String description, String venue) {
        this.ownerId = ownerId;
        this.moduleCode = moduleCode;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.venue = venue;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ownerId);
        dest.writeString(moduleCode);
        dest.writeString(description);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeString(venue);
    }

    private Listing(Parcel source) {
        this.ownerId = source.readString();
        this.moduleCode = source.readString();
        this.description = source.readString();
        this.startTime = source.readLong();
        this.endTime = source.readLong();
        this.venue = source.readString();
    }

    public static final Parcelable.Creator<Listing> CREATOR
            = new Parcelable.Creator<Listing>() {
        public Listing createFromParcel(Parcel in) {
            return new Listing(in);
        }
        public Listing[] newArray(int size) {
            return new Listing[size];
        }
    };

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
}
