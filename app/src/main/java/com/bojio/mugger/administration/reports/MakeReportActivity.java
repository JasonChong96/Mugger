package com.bojio.mugger.administration.reports;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class MakeReportActivity extends AppCompatActivity {

  private Map<String, Object> reportData;
  private Report.ReportType type;
  private FirebaseFirestore db;
  private FirebaseUser user;
  private AlertDialog dialog;

  @BindView(R.id.make_report_description)
  EditText descriptionView;

  @BindView(android.R.id.content)
  View view;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    db = FirebaseFirestore.getInstance();
    user = FirebaseAuth.getInstance().getCurrentUser();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_make_report);
    ButterKnife.bind(this);
    Bundle b = getIntent().getExtras();
    reportData = new HashMap<>();
    if (b == null) {
      Toasty.error(this, "Missing bundle data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    type = Report.ReportType.valueOf(b.getString("reportType"));
    type.transferData(b, reportData);
    reportData.put("type", type.name());
    dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setMessage("Submitting Report...")
        .setCancelable(false)
        .build();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @OnClick(R.id.make_report_submit_button)
  public void onClick_submit() {
    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    String reportDescription = descriptionView.getText().toString();
    if (reportDescription.isEmpty()) {
      Snacky.builder().setActivity(this).setText("Please fill in the report description.").error()
          .show();
      return;
    }
    dialog.show();
    reportData.put("reporterUid", user.getUid());
    reportData.put("reporterName", user.getDisplayName());
    reportData.put("time", System.currentTimeMillis());
    reportData.put("description", reportDescription);
    db.collection("reports").add(reportData).addOnCompleteListener(task -> {
      dialog.dismiss();
      if (!task.isSuccessful()) {
        Snacky.builder().setActivity(this).setText("Report submission failed. Please try again " +
            "later.").error()
            .show();
      } else {
        Toasty.success(this, "Report submitted successfully! Our moderators will be on it as " +
                "soon as possible.", Toast.LENGTH_SHORT, true).show();
        finish();
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }
}
