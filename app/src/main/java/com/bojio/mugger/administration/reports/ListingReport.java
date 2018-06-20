package com.bojio.mugger.administration.reports;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.widget.Button;

public class ListingReport extends Report {
  private String listingDescription;
  private String venue;
  private Button.OnClickListener listener;

  protected ListingReport(String uid, String complainantUid, String complaineeUid, String
      complaineeName, String complainantName, String description, long time, ReportType type,
                          String listingDescription,
                          String venue) {
    super(uid, complainantUid, complaineeUid, complaineeName, complainantName, description, time,
        type);
    this.listingDescription = listingDescription;
    this.venue = venue;
  }

  protected ListingReport() {
  }

  public ListingReport(Parcel in) {
    super(in);
    this.listingDescription = in.readString();
    this.venue = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeString(listingDescription);
    dest.writeString(venue);
  }

  public static final Creator<ListingReport> CREATOR = new Creator<ListingReport>() {
    @Override
    public ListingReport createFromParcel(Parcel in) {
      return new ListingReport(in);
    }

    @Override
    public ListingReport[] newArray(int size) {
      return new ListingReport[size];
    }
  };

  @Override
  public Button.OnClickListener getOnClickListener() {
    if (listener == null) {
      listener = v -> {
        Intent intent = new Intent(v.getContext(), ListingReportDetailsActivity.class);
        Bundle b = new Bundle();
        b.putParcelable("report", this);
        intent.putExtras(b);
        v.getContext().startActivity(intent);
      };
    }
    return listener;
  }

  public ListingReport(String listingDescription, String venue) {
    this.listingDescription = listingDescription;
    this.venue = venue;
  }

  public String getListingDescription() {
    return listingDescription;
  }

  public void setListingDescription(String listingDescription) {
    this.listingDescription = listingDescription;
  }

  public String getVenue() {
    return venue;
  }

  public void setVenue(String venue) {
    this.venue = venue;
  }
}
