package com.bojio.mugger.listings;

import android.app.AlertDialog;
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
import com.bojio.mugger.authentication.MuggerRole;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.fcm.MessagingService;
import com.bojio.mugger.listings.chat.ListingChatActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

  Listing listing;
  FirebaseAuth mAuth;
  FirebaseFirestore db;
  FirebaseMessaging fcm;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    fcm = FirebaseMessaging.getInstance();
    super.onCreate(savedInstanceState);
    if (stopActivity) {  finish();
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

    listing = b.getParcelable("listing");
    if (listing == null) {
      AlertDialog dialog = new SpotsDialog
          .Builder()
          .setContext(this)
          .setMessage("Loading listing information...")
          .setCancelable(false)
          .setTheme(R.style.SpotsDialog)
          .build();
      dialog.show();
      String listingUid = b.getString("listingUid");
      if (listingUid == null) {
        Toasty.error(this, "Missing Listing UID", Toast.LENGTH_SHORT).show();
        finish();
        return;
      }
      Task<DocumentSnapshot> listingTask = db.collection("listings").document(listingUid).get()
          .addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
              Toasty.error(this, "Error fetching listing data", Toast.LENGTH_SHORT).show();
              finish();
              return;
            } else {
              if (!task.getResult().exists()) {
                Toasty.info(this, "Listing no longer exists").show();
                finish();
                return;
              }
              listing = Listing.getListingFromSnapshot(task.getResult());
              init();
              dialog.dismiss();
            }
          });
    } else {
      init();
    }

    //  db.collection("listings").document(listing.getUid()).addSnapshotListener(this, )

  }

  @OnClick(R.id.chat_button)
  public void onClick() {
    Intent intent = new Intent(this, ListingChatActivity.class);
    Bundle b = new Bundle();
    b.putParcelable("listing", listing);
    intent.putExtras(b);
    startActivity(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.

    FirebaseUser user = mAuth.getCurrentUser();
    if (listing != null && (user.getUid().equals(listing.getOwnerId()) || MuggerRole.MODERATOR
        .check(MuggerUserCache.getInstance().getRole()))) {
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
      AlertDialog dialog = new SpotsDialog
          .Builder()
          .setContext(this)
          .setMessage("Deleting listing...")
          .setCancelable(false)
          .setTheme(R.style.SpotsDialog)
          .build();
      dialog.show();
      db.collection("listings").document(listing.getUid()).delete().addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
          Map<String, Object> notificationData = new HashMap<>();
          notificationData.put("title", "Listing Deleted");
          StringBuilder body = new StringBuilder();
          body.append(listing.getOwnerName()).append("'s ").append(listing.getModuleCode())
              .append(" Listing has been deleted.");
          notificationData.put("body", body.toString());
          notificationData.put("type", MessagingService.DELETED_NOTIFICATION);
          notificationData.put("fromUid", mAuth.getUid());
          notificationData.put("topicUid", listing.getUid());
          db.collection("notifications").add(notificationData);
          finish();
        } else {
          dialog.dismiss();
          Toasty.error(this, "Failed to delete listing, please try again later", Toast
              .LENGTH_SHORT).show();
        }
      });
    } else if (id == android.R.id.home) {
      finish();
    }

    return super.onOptionsItemSelected(item);
  }

  private void init() {
    java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(this);
    java.text.DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(this);
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
        DocumentReference listingRef = db.collection("listings").document(listing.getUid());
        Map<String, Object> updates = new HashMap<>();
        if (!isChecked) {
          if (mAuth.getUid().equals(listing.getOwnerId())) {
            Toasty.error(AvailableListingDetailsActivity.this, "You must be attending " +
                "listings " +
                "that you own.", Toast.LENGTH_SHORT).show();
            buttonView.setChecked(true);
          } else {
            updates.put(mAuth.getUid(), FieldValue.delete());
            fcm.unsubscribeFromTopic(listing.getUid());
            listingRef.update(updates);
          }
        } else {
          if (listing.getNumAttendees() > 19) {
            Toasty.error(AvailableListingDetailsActivity.this, "There are" +
                "too many people attending this listing", Toast.LENGTH_SHORT)
                .show();
            buttonView.setChecked(false);
          } else {
            updates.put(mAuth.getUid(), listing.getStartTime());
            fcm.subscribeToTopic(listing.getUid());
            listingRef.update(updates);
          }
        }
      }
    });
  }

  @OnClick(R.id.listing_details_report)
  public void onClick_report() {
    Intent intent = new Intent(this, MakeReportActivity.class);
    Bundle b = new Bundle();
    b.putParcelable("listing", listing);
    b.putString("reportType", Report.ReportType.LISTING.name());
    b.putString("listingUid", listing.getUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @OnClick(R.id.button_view_attendees)
  public void onClick_viewAttendees() {
    Intent intent = new Intent(this, ViewAttendeesActivity.class);
    Bundle b = new Bundle();
    b.putStringArrayList("profiles", (ArrayList<String>) listing.getAttendees());
    b.putString("ownerUid", listing.getOwnerId());
    intent.putExtras(b);
    startActivity(intent);
  }
}
