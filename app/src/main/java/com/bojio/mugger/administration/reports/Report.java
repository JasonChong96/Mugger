package com.bojio.mugger.administration.reports;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.bojio.mugger.listings.Listing;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public class Report implements Parcelable {
  protected ReportType type;
  protected String uid;
  protected String reporterUid;
  protected String reportedUid;
  protected String reportedName;
  protected String reporterName;
  protected String description;
  protected long time;
  protected String listingUid;

  protected Report(String uid, String reporterUid, String reportedUid, String reportedName,
                   String complainantName, String description, long time, ReportType type) {
    this.uid = uid;
    this.reporterUid = reporterUid;
    this.reportedUid = reportedUid;
    this.reportedName = reportedName;
    this.reporterName = complainantName;
    this.description = description;
    this.time = time;
    this.type = type;
  }

  protected Report() {
  }

  public Report(Parcel in) {
    uid = in.readString();
    reporterUid = in.readString();
    reportedUid = in.readString();
    reportedName = in.readString();
    reporterName = in.readString();
    description = in.readString();
    time = in.readLong();
    type = ReportType.valueOf(in.readString());
    listingUid = in.readString();
  }

  public static final Creator<Report> CREATOR = new Creator<Report>() {
    @Override
    public Report createFromParcel(Parcel in) {
      return new Report(in);
    }

    @Override
    public Report[] newArray(int size) {
      return new Report[size];
    }
  };

  public Button.OnClickListener getOnClickListener() {
    return v -> {
    };
  }

  @NonNull
  public static Report getReportFromSnapshot(DocumentSnapshot snapshot) {
    ReportType type = ReportType.valueOf((String) snapshot.get("type"));
    Report report = type.init(snapshot);
    report.setDescription((String) snapshot.get("description"));
    Long time = (Long) snapshot.get("time");
    report.setTime(time == null ? 0L : time);
    report.setReporterName((String) snapshot.get("reporterName"));
    report.setReportedName((String) snapshot.get("reportedName"));
    report.setReporterUid((String) snapshot.get("reporterUid"));
    report.setReportedUid((String) snapshot.get("reportedUid"));
    report.setListingUid((String) snapshot.get("listingUid"));
    report.setUid(snapshot.getId());
    return report;
  }

  public String getListingUid() {
    return listingUid;
  }

  public void setListingUid(String listingUid) {
    this.listingUid = listingUid;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getReporterUid() {
    return reporterUid;
  }

  public void setReporterUid(String reporterUid) {
    this.reporterUid = reporterUid;
  }

  public String getReportedUid() {
    return reportedUid;
  }

  public void setReportedUid(String reportedUid) {
    this.reportedUid = reportedUid;
  }

  public String getReportedName() {
    return reportedName;
  }

  public void setReportedName(String reportedName) {
    this.reportedName = reportedName;
  }

  public String getReporterName() {
    return reporterName;
  }

  public void setReporterName(String reporterName) {
    this.reporterName = reporterName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public ReportType getType() {
    return type;
  }

  public void setType(ReportType type) {
    this.type = type;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(uid);
    dest.writeString(reporterUid);
    dest.writeString(reportedUid);
    dest.writeString(reportedName);
    dest.writeString(reporterName);
    dest.writeString(description);
    dest.writeLong(time);
    dest.writeString(type.name());
    dest.writeString(listingUid);
  }

  public enum ReportType {
    LISTING {
      @Override
      Report init(DocumentSnapshot snapshot) {
        Report report = new ListingReport((String) snapshot.get("listingDescription"),
            (String) snapshot.get("venue"));
        report.setType(this);
        return report;
      }

      @Override
      public void transferData(Bundle b, Map<String, Object> reportData) {
        Listing listing = b.getParcelable("listing");
        reportData.put("listingDescription", listing.getDescription());
        reportData.put("venue", listing.getVenue());
        reportData.put("reportedUid", listing.getOwnerId());
        reportData.put("reportedName", listing.getOwnerName());
        reportData.put("listingUid", b.getString("listingUid"));
      }
    },
    CHAT {
      @Override
      Report init(DocumentSnapshot snapshot) {
        Report report = new ChatReport((String) snapshot.get("message"));
        report.setType(this);
        return report;
      }

      @Override
      public void transferData(Bundle b, Map<String, Object> reportData) {
        reportData.put("message", b.getString("message"));
        reportData.put("reportedUid", b.getString("reportedUid"));
        reportData.put("reportedName", b.getString("reportedName"));
        reportData.put("listingUid", b.getString("listingUid"));
      }
    };
    abstract Report init(DocumentSnapshot snapshot);
    public abstract void transferData(Bundle b, Map<String, Object> reportData);
  }
}
