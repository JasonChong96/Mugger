package com.bojio.mugger.administration.reports;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bojio.mugger.R;
import com.bojio.mugger.listings.chat.ListingChatActivity;
import com.bojio.mugger.profile.ProfileActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class ChatReportDetailsActivity extends AppCompatActivity {

  @BindView(R.id.chat_report_reported_name)
  TextView reportedNameView;

  @BindView(R.id.chat_report_reporter_name)
  TextView reporterNameView;

  @BindView(R.id.chat_report_message)
  TextView messageView;

  @BindView(R.id.chat_report_description)
  TextView descriptionView;

  ChatReport report;
  FirebaseFirestore db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    db = FirebaseFirestore.getInstance();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat_report_details);
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    Bundle b = getIntent().getExtras();
    if (b == null) {
      finish();
      Toasty.error(this, "Missing bundle data").show();
      return;
    }
    report = b.getParcelable("report");
    reportedNameView.setText(report.getReportedName());
    reporterNameView.setText(String.format("Reported by %s", report.getReporterName()));
    messageView.setText(report.getMessage());
    descriptionView.setText(report.getDescription());
  }

  @OnClick(R.id.chat_report_chat_button)
  public void onClick_chat() {
    Intent intent = new Intent(this, ListingChatActivity.class);
    Bundle b = new Bundle();
    b.putString("listingUid", report.getListingUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @OnClick(R.id.chat_report_reported_profile_button)
  public void onClick_reported() {
    Intent intent = new Intent(this, ProfileActivity.class);
    Bundle b = new Bundle();
    b.putString("profileUid", report.getReportedUid());
    intent.putExtras(b);
    startActivity(intent);
  }

  @OnClick(R.id.chat_report_reporter_profile_button)
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.

      getMenuInflater().inflate(R.menu.listing_menu, menu);
      menu.findItem(R.id.edit_listing).setVisible(false);

    return true;
  }
}
