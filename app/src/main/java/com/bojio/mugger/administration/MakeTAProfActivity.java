package com.bojio.mugger.administration;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.constants.ModuleRole;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class MakeTAProfActivity extends LoggedInActivity {

  @BindView(R.id.make_ta_prof_button_submit)
  Button submitButton;
  @BindView(R.id.make_ta_prof_radio_prof)
  RadioButton checkBoxProf;
  @BindView(R.id.make_ta_prof_radio_ta)
  RadioButton checkBoxTA;
  @BindView(R.id.make_ta_prof_radiogroup)
  RadioGroup radioGroup;
  @BindView(R.id.make_ta_prof_edit_text_module)
  EditText editTextModule;
  @BindView(R.id.make_ta_prof_semester)
  TextView semesterView;
  @BindView(R.id.make_ta_prof_title)
  TextView titleView;
  private FirebaseFirestore db;
  private byte newRole;
  private String userUid;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    db = FirebaseFirestore.getInstance();
    super.onCreate(savedInstanceState);
    if (stopActivity) {  finish();
      return;
    }
    setContentView(R.layout.activity_make_taprof);
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    AlertDialog dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setMessage("Fetching current semester...")
        .setTheme(R.style.SpotsDialog)
        .setCancelable(false)
        .build();
    dialog.show();
    Bundle b = getIntent().getExtras();
    if (b == null) {
      Toasty.error(this, "Error: Missing user data", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    userUid = b.getString("userUid");
    db.collection("data").document("otherData").get().addOnCompleteListener(task -> {
      if (!task.isSuccessful()) {
        Toasty.error(this, "Error: Failed to fetch current semester", Toast.LENGTH_SHORT)
            .show();
        finish();
      } else {
        semesterView.setText(((String) task.getResult().get("currentSem")).replace(".", "/"));
        dialog.dismiss();
      }
    });
    titleView.setText(b.getString("name"));
    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
          case R.id.make_ta_prof_radio_prof:
            newRole = ModuleRole.PROFESSOR;
            break;
          case R.id.make_ta_prof_radio_ta:
            newRole = ModuleRole.TEACHING_ASSISTANT;
            break;
          case R.id.make_ta_prof_radio_remove:
            newRole = ModuleRole.REMOVE;
            break;
        }
      }
    });
  }

  @OnClick(R.id.make_ta_prof_button_submit)
  void onClick_submit() {
    String module = editTextModule.getText().toString();
    if (module.isEmpty()) {
      Toasty.error(this, "Please fill in a module code.", Toast.LENGTH_SHORT).show();
    } else if (newRole == ModuleRole.EMPTY) {
      Toasty.error(this, "Please choose a role.", Toast.LENGTH_SHORT).show();
    } else {
      AlertDialog dialog = new SpotsDialog
          .Builder()
          .setContext(this)
          .setMessage("Changing role...")
          .setTheme(R.style.SpotsDialog)
          .setCancelable(false)
          .build();
      dialog.show();
      Boolean remove = newRole == ModuleRole.REMOVE;
      String role = newRole == ModuleRole.PROFESSOR ? "professor" : "ta";
      DocumentReference docRef = db.collection("users").document(userUid).collection("semesters")
          .document(semesterView.getText().toString().replace("/", "."));
      docRef.get().addOnCompleteListener(task -> {
        if (!task.isSuccessful()) {
          dialog.dismiss();
          Toasty.error(this, "Error, please try again", Toast.LENGTH_SHORT).show();
        } else {
          if (!remove) {
            List<String> existing = (List<String>) task.getResult().getData().get(role);
            if (existing == null) {
              existing = new ArrayList<>();
            }
            if (!existing.contains(module)) {
              existing.add(module);
            }
            Collections.sort(existing);
            Map<String, Object> data = new HashMap<>();
            data.put(role, existing);
            docRef.set(data, SetOptions.merge()).addOnCompleteListener(taskk -> {
              dialog.dismiss();
              if (!taskk.isSuccessful()) {
                Toasty.error(this, "Error, please try again", Toast.LENGTH_SHORT).show();
              } else {
                Toasty.success(this, "Successfully updated. Please get the user to relogin to " +
                        "Mugger",
                    Toast.LENGTH_SHORT).show();
                finish();
              }
            });
          } else {
            Map<String, Object> data = new HashMap<>();
            if (task.getResult().exists()) {
              List<String> ta = (List<String>) task.getResult().getData().get("ta");
              List<String> prof = (List<String>) task.getResult().getData().get("professor");
              if (ta != null) {
                if (ta.remove(module)) {
                  data.put("ta", ta);
                }
              }
              if (prof != null) {
                if (prof.remove(module)) {
                  data.put("professor", prof);
                }
              }
            }
            docRef.set(data, SetOptions.merge()).addOnCompleteListener(taskk -> {
              dialog.dismiss();
              if (!taskk.isSuccessful()) {
                Toasty.error(this, "Error, please try again", Toast.LENGTH_SHORT).show();
              } else {
                Toasty.success(this, "Successfully updated. Please reload this profile page to " +
                    "view changes and get the user to relogin for the effects to take place.", Toast
                    .LENGTH_SHORT).show();
                finish();
              }
            });
          }
        }
      });
    }
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
