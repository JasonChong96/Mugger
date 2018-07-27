package com.bojio.mugger.administration.reports;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bojio.mugger.R;
import com.bojio.mugger.administration.reports.viewmodels.ListingReportViewModel;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.listings.AvailableListingDetailsActivity;
import com.bojio.mugger.profile.ProfileActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class ListingReportDetailsActivity extends LoggedInActivity {

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

  private ListingReportViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (stopActivity) {
      finish();
      return;
    }
    setContentView(R.layout.activity_listing_report_details);
    mViewModel = ViewModelProviders.of(this).get(ListingReportViewModel.class);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ButterKnife.bind(this);
    Bundle b = getIntent().getExtras();
    if (b == null) {
      finish();
      Toasty.error(this, "Bundle not found").show();
      return;
    }
    if (savedInstanceState == null) {
      mViewModel.init(b.getParcelable("report"));
    }
    reportedNameView.setText(mViewModel.getReportedName());
    reporterNameView.setText(String.format("Reported By %s", mViewModel.getReporterName()));
    reportDescriptionView.setText(mViewModel.getReportDescription());
    listingDescriptionView.setText(mViewModel.getListingDescription());
    venueView.setText(mViewModel.getListingVenue());

  }

  @OnClick(R.id.listing_report_details_listing_button)
  public void onClick_listing() {
    Intent intent = new Intent(this, AvailableListingDetailsActivity.class);
    Bundle b = new Bundle();
    b.putString("listingUid", mViewModel.getListingUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @OnClick(R.id.listing_report_details_reported_profile_button)
  public void onClick_reported() {
    Intent intent = new Intent(this, ProfileActivity.class);
    Bundle b = new Bundle();
    b.putString("profileUid", mViewModel.getReportedUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @OnClick(R.id.listing_report_details_reporter_profile_button)
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
        mViewModel.deleteReport().addOnCompleteListener(task -> {
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
