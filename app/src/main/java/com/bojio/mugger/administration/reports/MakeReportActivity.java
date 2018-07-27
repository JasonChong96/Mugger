package com.bojio.mugger.administration.reports;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class MakeReportActivity extends LoggedInActivity {

  @BindView(R.id.make_report_description)
  EditText descriptionView;
  @BindView(android.R.id.content)
  View view;
  private Map<String, Object> reportData;
  private Report.ReportType type;
  private AlertDialog dialog;
  private MakeReportViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (stopActivity) {
      finish();
      return;
    }
    setContentView(R.layout.activity_make_report);
    ButterKnife.bind(this);
    Bundle b = getIntent().getExtras();
    reportData = new HashMap<>();
    if (b == null) {
      Toasty.error(this, "Missing bundle data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    mViewModel = ViewModelProviders.of(this).get(MakeReportViewModel.class);
    mViewModel.init(b);
    dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setMessage("Submitting Report...")
        .setCancelable(false)
        .setTheme(R.style.SpotsDialog)
        .build();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @OnClick(R.id.make_report_submit_button)
  public void onClick_submit() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    String reportDescription = descriptionView.getText().toString().trim();
    if (reportDescription.isEmpty()) {
      Snacky.builder().setActivity(this).setText("Please fill in the report description.").error()
          .show();
      return;
    }
    dialog.show();
    mViewModel.submitReport(reportDescription).addOnCompleteListener(task -> {
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
