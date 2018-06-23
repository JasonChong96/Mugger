package com.bojio.mugger.administration;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.MuggerUser;
import com.bojio.mugger.constants.MuggerRole;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class ChangeMuggerRoleActivity extends AppCompatActivity {

  @BindView(R.id.change_role_title)
  TextView titleView;

  @BindView(R.id.change_role_spinner)
  Spinner spinner;

  @BindView(R.id.change_role_constraint_layout)
  ConstraintLayout layout;

  String uid;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_change_mugger_role);
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    Bundle b = getIntent().getExtras();
    if (b == null) {
      Toasty.error(this, "Error missing bundle data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    uid = b.getString("uid");
    List<String> availableRoles = new ArrayList<>();
    MuggerRole ownRole = MuggerUser.getInstance().getRole();
    for (MuggerRole role : MuggerRole.values()) {
      if (ownRole.checkSuperiorityTo(role)) {
        availableRoles.add(role.name());
      }
    }
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
        availableRoles);
    spinner.setAdapter(adapter);
    spinner.setSelection(Math.max(0, availableRoles.indexOf(
        MuggerRole.getByRoleId(b.getInt(("currentRole"))).name())));
  }

  @OnClick(R.id.change_role_submit)
  void onClick_submit() {
    AlertDialog dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setMessage("Changing role...")
        .setCancelable(false)
        .setTheme(R.style.SpotsDialog)
        .build();
    dialog.show();
    long roleId = MuggerRole.valueOf((String) spinner.getSelectedItem()).getRoleId();
    DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(uid);
    docRef.update("roleId",
        roleId).addOnCompleteListener
        (task -> {
          dialog.dismiss();
          if (!task.isSuccessful()) {
            Snackbar.make(layout, "Failed to update, please try again.", Snackbar.LENGTH_SHORT)
                .show();
          } else {
            Toasty.info(this, "Successfully updated.", Toast
                .LENGTH_SHORT).show();
            docRef.get().addOnSuccessListener(taskk -> {
              String instanceId = (String) taskk.get("instanceId");
              Map<String, Object> notificationData = new HashMap<>();
              notificationData.put("instanceId", instanceId);
              notificationData.put("fromUid", "");
              notificationData.put("topicUid", "");
              notificationData.put("type", "role");
              notificationData.put("newRoleName", spinner.getSelectedItem());
              FirebaseFirestore.getInstance().collection("notifications").add(notificationData);
            });

            finish();
          }
        });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // When back button on the top left is clicked
      case android.R.id.home:
        finish();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
