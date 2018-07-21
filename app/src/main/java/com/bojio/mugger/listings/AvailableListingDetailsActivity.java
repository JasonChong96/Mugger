package com.bojio.mugger.listings;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.administration.reports.MakeReportActivity;
import com.bojio.mugger.administration.reports.Report;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.lifecycle.LifecycleUtils;
import com.bojio.mugger.listings.chat.ListingChatActivity;
import com.bojio.mugger.listings.viewmodels.ListingDetailsViewModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class AvailableListingDetailsActivity extends LoggedInActivity {

  @BindView(R.id.module_code)
  TextView moduleCode;

  @BindView(R.id.start_time)
  TextView startDateTime;

  @BindView(R.id.end_time)
  TextView endDateTime;

  @BindView(R.id.location)
  TextView venue;

  @BindView(R.id.description)
  TextView description;

  @BindView(R.id.num_attendees)
  TextView numAttendees;

  @BindView(R.id.is_attending)
  CheckBox isAttending;

  @BindView(R.id.progressBar3)
  ProgressBar progressBar;

  private ListingDetailsViewModel mViewModel;
  private AlertDialog dialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mViewModel = ViewModelProviders.of(this, LifecycleUtils.getAndroidViewModelFactory
        (getApplication())).get(ListingDetailsViewModel.class);
    super.onCreate(savedInstanceState);
    if (stopActivity) {
      finish();
      return;
    }
    setContentView(R.layout.activity_available_listing_details);
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    Bundle b = getIntent().getExtras();
    if (b == null) {
      finish();
      Toasty.error(this, "Error fetching listing details.", Toast.LENGTH_SHORT).show();
      return;
    }
    String listingUid = b.getString("listingUid");
    if (listingUid == null) {
      Toasty.error(this, "Missing Listing UID", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setMessage("Loading listing information...")
        .setCancelable(false)
        .setTheme(R.style.SpotsDialog)
        .build();
    dialog.show();
    mViewModel.init(listingUid);
    mViewModel.getStartTimeString().observe(this, startDateTime::setText);
    mViewModel.getEndTimeString().observe(this, endDateTime::setText);
    mViewModel.getVenue().observe(this, venue::setText);
    mViewModel.getDescription().observe(this, desc -> {
      description.setText(desc);
      if (dialog.isShowing()) {
        moduleCode.setText(mViewModel.getModuleCode());
        dialog.dismiss();
      }
    });
    mViewModel.getNumAttendees().observe(this, num -> {
      if (num != null) {
        numAttendees.setText(new StringBuilder(Integer.toString(num))
            .append(" ")
            .append(num > 1 ? "people" : "person")
            .append(" attending"));
      }
    });
    mViewModel.getIsAttending().observe(this, isAttending::setChecked);
    mViewModel.getDeleted().observe(this, deleted -> {
      if (deleted) {
        Toasty.info(this, "Listing no longer exists").show();
        finish();
        return;
      }
    });
    setCheckedChangeListener();
  }

  @OnClick(R.id.chat_button)
  public void onClick() {
    Intent intent = new Intent(this, ListingChatActivity.class);
    Bundle b = new Bundle();
    b.putParcelable("listing", mViewModel.getListing());
    intent.putExtras(b);
    startActivity(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    mViewModel.getDescription().observe(this, desc -> {
      description.setText(desc);
      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      if (desc != null) {
        if (mViewModel.canEditDelete()) {
          getMenuInflater().inflate(R.menu.listing_menu, menu);
        }
      }
    });
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.edit_listing) {
      Intent intent = new Intent(this, CreateEditListingActivity.class);
      Bundle b = new Bundle();
      b.putParcelable("listing", mViewModel.getListing());
      intent.putExtras(b);
      startActivity(intent);
      finish();
    } else if (id == R.id.delete_listing) {
      AlertDialog dialog = new SpotsDialog
          .Builder()
          .setContext(this)
          .setMessage("Deleting listing...")
          .setCancelable(false)
          .setTheme(R.style.SpotsDialog)
          .build();
      dialog.show();
      mViewModel.deleteListing().addOnCompleteListener(task -> {
        dialog.dismiss();
        if (task.isSuccessful()) {
          finish();
        } else {
          Snacky.builder()
              .setActivity(this)
              .setText("Failed to delete listing, please try again later")
              .error()
              .show();
        }
      });
    } else if (id == android.R.id.home) {
      finish();
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Set the operations to carry out when the attending checkbox's state is toggled
   */
  private void setCheckedChangeListener() {
    isAttending.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (!isChecked) {
        if (mViewModel.isOwnListing()) {
          Toasty.error(AvailableListingDetailsActivity.this, "You must be attending " +
              "listings that you own.", Toast.LENGTH_SHORT).show();
          buttonView.setChecked(true);
        } else {
          mViewModel.unjoinListing();
        }
      } else {
        if (mViewModel.listingFull()) {
          Toasty.error(AvailableListingDetailsActivity.this, "There are" +
              "too many people attending this listing", Toast.LENGTH_SHORT)
              .show();
          buttonView.setChecked(false);
        } else {
          mViewModel.joinListing();
        }
      }
    });
  }

  @OnClick(R.id.listing_details_report)
  public void onClick_report() {
    Intent intent = new Intent(this, MakeReportActivity.class);
    Bundle b = new Bundle();
    b.putParcelable("listing", mViewModel.getListing());
    b.putString("reportType", Report.ReportType.LISTING.name());
    b.putString("listingUid", mViewModel.getListingUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @OnClick(R.id.button_view_attendees)
  public void onClick_viewAttendees() {
    Intent intent = new Intent(this, ViewAttendeesActivity.class);
    Bundle b = new Bundle();
    b.putStringArrayList("profiles", (ArrayList<String>) mViewModel.getAttendees());
    b.putString("ownerUid", mViewModel.getOwnerUid());
    intent.putExtras(b);
    startActivity(intent);
  }
}