package com.bojio.mugger.administration.feedback;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class MakeFeedbackActivity extends LoggedInActivity {

  @BindView(R.id.make_feedback_title)
  EditText titleView;

  @BindView(R.id.make_feedback_description)
  EditText descriptionView;

  @BindView(android.R.id.content)
  View view;

  private MakeFeedbackViewModel mViewModel;
  private AlertDialog dialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (stopActivity) {
      finish();
      return;
    }
    setContentView(R.layout.activity_make_feedback);
    mViewModel = ViewModelProviders.of(this).get(MakeFeedbackViewModel.class);
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    db = FirebaseFirestore.getInstance();
    dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setMessage("Submitting Feedback...")
        .setCancelable(false)
        .setTheme(R.style.SpotsDialog)
        .build();
  }

  @OnClick(R.id.make_feedback_button)
  public void onClick_submit() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    String title = titleView.getText().toString().trim();
    String description = descriptionView.getText().toString().trim();
    if (title.isEmpty()) {
      Snackbar.make(view, "Please fill in a title.", Snackbar.LENGTH_SHORT).show();
      return;
    }
    if (description.isEmpty()) {
      Snackbar.make(view, "Please fill in a description.", Snackbar.LENGTH_SHORT).show();
      return;
    }
    dialog.show();
    mViewModel.submitFeedback(title, description).addOnCompleteListener(task -> {
      if (!task.isSuccessful()) {
        dialog.dismiss();
        Snacky.builder().setActivity(this).setText("Failed to submit feedback, please try again" +
            " later.").error().show();
      } else {
        Toasty.success(this, "Successfully submitted feedback.", Toast.LENGTH_SHORT).show();
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
