package com.bojio.mugger.administration.reports.viewmodels;

import android.arch.lifecycle.ViewModel;

import com.bojio.mugger.administration.reports.ChatReport;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatReportViewModel extends ViewModel {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private ChatReport report;

  public ChatReportViewModel() {
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
  }

  public void init(ChatReport report) {
    this.report = report;
  }

  public String getReportedName() {
    return report.getReportedName();
  }

  public String getReporterName() {
    return report.getReporterName();
  }

  public String getReportDescription() {
    return report.getDescription();
  }

  public String getMessage() {
    return report.getMessage();
  }

  public String getListingUid() {
    return report.getListingUid();
  }

  public String getReportedUid() {
    return report.getReportedUid();
  }

  public String getReporterUid() {
    return report.getReporterUid();
  }

  public Task<Void> deleteReport() {
    return MuggerDatabase.deleteReport(db, report.getUid());
  }
}
