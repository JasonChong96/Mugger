package com.bojio.mugger.listings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.profile.ProfileListRecyclerAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class ViewAttendeesActivity extends LoggedInActivity {

  @BindView(R.id.attendees_list_recycler)
  RecyclerView recyclerView;

  private CollectionReference colRef;
  private ArrayList<String> profileIds;
  private ArrayList<DocumentSnapshot> profiles;
  private AlertDialog dialog;
  private String ownerUid;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (stopActivity) {
      finish();
      return;
    }
    setContentView(R.layout.activity_view_attendees);
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    dialog = new SpotsDialog.Builder()
        .setContext(this)
        .setMessage("Loading data...")
        .setCancelable(false)
        .setTheme(R.style.SpotsDialog)
        .build();
    dialog.show();
    Bundle b = getIntent().getExtras();
    if (b == null) {
      Toasty.error(this, "Error, missing attendees' information.").show();
      finish();
      return;
    }
    ownerUid = b.getString("ownerUid");
    profileIds = b.getStringArrayList("profiles");
    colRef = MuggerDatabase.getAllUsersReference(db);
    profiles = new ArrayList<>();
    loadProfiles();
  }

  /**
   * Loads profile data of all attendees and initializes the RecyclerView to display them. If it
   * fails, the activity will be finished and a toast message will be shown to indicate an error
   * to the user.
   */
  private void loadProfiles() {
    List<Task<?>> tasks = new ArrayList<>();
    for (String id : profileIds) {
      tasks.add(colRef.document(id).get().addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
          profiles.add(task.getResult());
        } else {
          Toasty.error(this, "Error loading attendees' data.").show();
          return;
        }
      }));
    }
    Tasks.whenAll(tasks).addOnCompleteListener(task -> {
      dialog.dismiss();
      if (!task.isSuccessful()) {
        Toasty.error(this, "Error fetching profile data").show();
        finish();
      } else {
        // Setup view for profile list.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ProfileListRecyclerAdapter adapter = new ProfileListRecyclerAdapter(profiles, ownerUid);
        recyclerView.setAdapter(adapter);
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // When back button on the top left is clicked
      case android.R.id.home:
        onBackPressed();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
