package com.bojio.mugger.administration.reports;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.bojio.mugger.R;
import com.bojio.mugger.listings.AvailableListingDetailsActivity;
import com.bojio.mugger.profile.ProfileActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class ListingReportDetailsActivity extends AppCompatActivity {

  @BindView(R.id.listing_report_details_listing_description)
  TextView listingDescriptionView;

  @BindView(R.id.listing_report_details_report_description)
  TextView reportDescriptionView;

  @BindView(R.id.listing_report_details_reported_name)
  TextView reportedNameView;

  @BindView(R.id.listing_report_details_reporter_name)
  TextView reporterNameView;

  @BindView(R.id.listing_report_details_venue)
  TextView venueView;

  ListingReport report;
  FirebaseFirestore db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    db = FirebaseFirestore.getInstance();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_listing_report_details);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ButterKnife.bind(this);
    Bundle b = getIntent().getExtras();
    if (b == null) {
      finish();
      Toasty.error(this, "Bundle not found").show();
      return;
    }
    report = b.getParcelable("report");
    reportedNameView.setText(report.getReportedName());
    reporterNameView.setText(String.format("Reported By %s", report.getReporterName()));
    reportDescriptionView.setText(report.getDescription());
    listingDescriptionView.setText(report.getListingDescription());
    venueView.setText(report.getVenue());
  }

  @OnClick(R.id.listing_report_details_listing_button)
  public void onClick_listing() {
    Intent intent = new Intent(this, AvailableListingDetailsActivity.class);
    Bundle b = new Bundle();
    b.putString("listingUid", report.getListingUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @OnClick(R.id.listing_report_details_reported_profile_button)
  public void onClick_reported() {
    Intent intent = new Intent(this, ProfileActivity.class);
    Bundle b = new Bundle();
    b.putString("profileUid", report.getReportedUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @OnClick(R.id.listing_report_details_reporter_profile_button)
  public void onClick_reporter() {
    Intent intent = new Intent(this, ProfileActivity.class);
    Bundle b = new Bundle();
    b.putString("profileUid", report.getReporterUid());
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
            .build();
        dialog.show();
        db.collection("reports").document(report.getUid()).delete().addOnCompleteListener(task
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
}
