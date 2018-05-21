package com.bojio.mugger.listings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.fragments.MyListingsFragments;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvailableListingDetailsActivity extends AppCompatActivity {

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

  Listing listing;
  FirebaseAuth mAuth;
  FirebaseFirestore db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_available_listing_details);
    ButterKnife.bind(this);
    Bundle b = getIntent().getExtras();
    if (b == null) {
      finish();
      Toast.makeText(this, "Error fetching listing details.", Toast.LENGTH_SHORT);
      return;
    }
    java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(this);
    java.text.DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(this);
    listing = b.getParcelable("listing");
    moduleCode.setText(listing.getModuleCode());
    Date startDate = new Date(listing.getStartTime());
    Date endDate = new Date(listing.getEndTime());
    startDateTime.setText(new StringBuilder()
        .append(df.format(startDate))
        .append(" ")
        .append(dfTime.format(startDate))
        .toString());
    endDateTime.setText(new StringBuilder()
        .append(df.format(endDate))
        .append(" ")
        .append(dfTime.format(endDate))
        .toString());
    venue.setText(listing.getVenue());
    description.setText(listing.getDescription());
    numAttendees.setText(new StringBuilder(Long.toString(listing.getNumAttendees()))
        .append(" ")
        .append(listing.getNumAttendees() > 1 ? "people" : "person")
        .append(" attending"));
    isAttending.setChecked(listing.isAttending(mAuth.getUid()));
    isAttending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mAuth.getUid().equals(listing.getOwnerId()) && !isChecked) {
          Toast.makeText(AvailableListingDetailsActivity.this, "You must be attending listings " +
              "that you own.", Toast.LENGTH_SHORT).show();
          buttonView.setChecked(true);
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.

    FirebaseUser user = mAuth.getCurrentUser();
    if (user.getUid().equals(listing.getOwnerId())) {
      getMenuInflater().inflate(R.menu.listing_menu, menu);
    }
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
      intent.putExtras(this.getIntent());
      startActivity(intent);
      finish();
    } else if (id == R.id.delete_listing) {
      progressBar.setVisibility(View.VISIBLE);
      db.collection("listings").document(listing.getUid()).delete().addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
          finish();
        } else {
          progressBar.setVisibility(View.GONE);
          Toast.makeText(this, "Failed to delete listing, please try again later", Toast
              .LENGTH_SHORT);
        }
      });
    }

    return super.onOptionsItemSelected(item);
  }
}