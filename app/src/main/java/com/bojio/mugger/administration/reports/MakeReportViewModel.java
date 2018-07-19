package com.bojio.mugger.administration.reports;

import android.arch.lifecycle.ViewModel;
import android.os.Bundle;

import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MakeReportViewModel extends ViewModel {
  private FirebaseAuth mAuth;
  private FirebaseFirestore db;
  private Map<String, Object> reportData;
  private Report.ReportType type;

  public MakeReportViewModel() {
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    reportData = new HashMap<>();
  }

  public void init(Bundle b) {
    if (reportData.isEmpty()) {
      type = Report.ReportType.valueOf(b.getString("reportType"));
      type.transferData(b, reportData);
      reportData.put("type", type.name());
    }
  }

  public Task<DocumentReference> submitReport(String reportDescription) {
    reportData.put("reporterUid", mAuth.getUid());
    reportData.put("reporterName", mAuth.getCurrentUser().getDisplayName());
    reportData.put("time", System.currentTimeMillis());
    reportData.put("description", reportDescription);
    return MuggerDatabase.sendReport(db, reportData);
  }
}
