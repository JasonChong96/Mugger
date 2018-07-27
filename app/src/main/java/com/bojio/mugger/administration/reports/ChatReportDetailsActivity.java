package com.bojio.mugger.administration.reports;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bojio.mugger.R;
import com.bojio.mugger.administration.reports.viewmodels.ChatReportViewModel;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.listings.AvailableListingDetailsActivity;
import com.bojio.mugger.profile.ProfileActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class ChatReportDetailsActivity extends LoggedInActivity {

  @BindView(R.id.chat_report_reported_name)
  TextView reportedNameView;

  @BindView(R.id.chat_report_reporter_name)
  TextView reporterNameView;

  @BindView(R.id.chat_report_message)
  TextView messageView;

  @BindView(R.id.chat_report_description)
  TextView descriptionView;

  private ChatReportViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (stopActivity) {
      finish();
      return;
    }
    setContentView(R.layout.activity_chat_report_details);
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    Bundle b = getIntent().getExtras();
    if (b == null) {
      finish();
      Toasty.error(this, "Missing bundle data").show();
      return;
    }
    mViewModel = ViewModelProviders.of(this).get(ChatReportViewModel.class);
    if (savedInstanceState == null) {
      mViewModel.init(b.getParcelable("report"));
    }
    reportedNameView.setText(mViewModel.getReportedName());
    reporterNameView.setText(String.format("Reported by %s", mViewModel.getReporterName()));
    messageView.setText(mViewModel.getMessage());
    descriptionView.setText(mViewModel.getReportDescription());

  }

  @OnClick(R.id.chat_report_chat_button)
  public void onClick_chat() {
    Intent intent = new Intent(this, AvailableListingDetailsActivity.class);
    Bundle b = new Bundle();
    b.putString("listingUid", mViewModel.getListingUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @OnClick(R.id.chat_report_reported_profile_button)
  public void onClick_reported() {
    Intent intent = new Intent(this, ProfileActivity.class);
    Bundle b = new Bundle();
    b.putString("profileUid", mViewModel.getReportedUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @OnClick(R.id.chat_report_reporter_profile_button)
  public void onClick_reporter() {
    Intent intent = new Intent(this, ProfileActivity.class);
    Bundle b = new Bundle();
    b.putString("profileUid", mViewModel.getReporterUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // When back button on the top left is clicked
      case android.R.id.home:
        onBackPressed();
        break;
      case R.id.delete_listing:
        AlertDialog dialog = new SpotsDialog
            .Builder()
            .setContext(this)
            .setMessage("Deleting report...")
            .setCancelable(false)
            .setTheme(R.style.SpotsDialog)
            .build();
        dialog.show();
        mViewModel.deleteReport().addOnCompleteListener(task
            -> {
          dialog.dismiss();
          if (!task.isSuccessful()) {
            Snacky.builder()
                .setActivity(this)
                .setText("Error deleting report. Please try again later.")
                .error()
                .show();
          } else {
            finish();
            Toasty.success(this, "The report has been successfully deleted!").show();
          }
        });
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.

    getMenuInflater().inflate(R.menu.listing_menu, menu);
    menu.findItem(R.id.edit_listing).setVisible(false);

    return true;
  }
}
