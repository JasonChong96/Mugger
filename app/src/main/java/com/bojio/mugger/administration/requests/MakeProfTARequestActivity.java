package com.bojio.mugger.administration.requests;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class MakeProfTARequestActivity extends AppCompatActivity {
  private static String[] roles = {"Click here to choose a role.", "Teaching Assistant",
      "Professor"};
  FirebaseUser user;
  FirebaseFirestore db;
  @BindView(R.id.request_profta_button)
  Button submitButton;
  @BindView(R.id.request_profta_description)
  EditText descriptionView;
  @BindView(R.id.request_profta_module_code)
  EditText moduleCodeView;
  @BindView(R.id.request_profta_role_spinner)
  Spinner roleSpinner;
  @BindView(android.R.id.content)
  View view;
  private AlertDialog dialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    db = FirebaseFirestore.getInstance();
    user = FirebaseAuth.getInstance().getCurrentUser();
    if (user == null) {
      finish();
      Toasty.info(this, "Not logged in", Toast.LENGTH_SHORT).show();
      return;
    }
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_make_prof_tarequest);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ButterKnife.bind(this);
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout
        .simple_dropdown_item_1line, roles);
    roleSpinner.setAdapter(adapter);
    dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setMessage("Submitting Request...")
        .setCancelable(false)
        .setTheme(R.style.SpotsDialog)
        .build();
  }

  @OnClick(R.id.request_profta_button)
  public void onClick_submit() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    String moduleCode = moduleCodeView.getText().toString();
    String description = descriptionView.getText().toString();
    if (roleSpinner.getSelectedItemPosition() == 0) {
      Snacky.builder().setActivity(this)
          .setText("Please choose your role in the module.")
          .error().show();
      return;
    }
    if (moduleCode.isEmpty()) {
      Snacky.builder().setActivity(this)
          .setText("Please enter the module code.")
          .error().show();
      return;
    }
    if (description.isEmpty()) {
      Snacky.builder().setActivity(this)
          .setText("Please fill in the description field.")
          .error().show();
      return;
    }
    dialog.show();
    Map<String, Object> request = new HashMap<>();
    request.put("time", System.currentTimeMillis());
    request.put("moduleCode", moduleCode);
    request.put("description", description);
    request.put("role", roleSpinner.getSelectedItem().toString());
    request.put("userUid", user.getUid());
    request.put("userName", user.getDisplayName());
    db.collection("requestsProfTA").add(request).addOnCompleteListener(task -> {
      if (!task.isSuccessful()) {
        dialog.dismiss();
        Snackbar.make(view, "Failed to submit request, please try again later.", Snackbar
            .LENGTH_SHORT).show();
      } else {
        Toasty.success(this, "Successfully submitted request.", Toast.LENGTH_SHORT)
            .show();
        finish();
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }
}
