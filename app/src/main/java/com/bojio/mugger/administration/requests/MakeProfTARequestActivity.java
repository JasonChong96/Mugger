package com.bojio.mugger.administration.requests;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class MakeProfTARequestActivity extends LoggedInActivity {
  private static String[] roles = {"Click here to choose a role.", "Teaching Assistant",
      "Professor"};
  @BindView(R.id.request_profta_description)
  EditText descriptionView;
  @BindView(R.id.request_profta_module_code)
  EditText moduleCodeView;
  @BindView(R.id.request_profta_role_spinner)
  Spinner roleSpinner;
  @BindView(android.R.id.content)
  View view;
  private AlertDialog dialog;
  private MakeProfTARequestViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (stopActivity) {
      finish();
      return;
    }
    mViewModel = ViewModelProviders.of(this).get(MakeProfTARequestViewModel.class);
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
      Snacky.builder()
          .setActivity(this)
          .setText("Please choose your role in the module.")
          .error()
          .show();
      return;
    }
    if (isFieldEmpty(moduleCode, "Please fill in the module code.")) {
      return;
    }
    if (isFieldEmpty(description, "Please fill in the description field.")) {
      return;
    }
    dialog.show();
    mViewModel.submitRequest(moduleCode, description, roleSpinner.getSelectedItem().toString()).addOnCompleteListener(task -> {
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

  private boolean isFieldEmpty(String toCheck, String errorMsg) {
    if (toCheck.isEmpty()) {
      Snacky.builder().setActivity(this)
          .setText(errorMsg)
          .error().show();
      return true;
    }
    return false;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }
}
