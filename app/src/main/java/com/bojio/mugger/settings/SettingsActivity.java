package com.bojio.mugger.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.fcm.MessagingService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class SettingsActivity extends LoggedInActivity {

  @BindView(R.id.settings_change_display_name_button)
  Button buttonChangeName;

  @BindView(R.id.settings_change_display_name_edittext)
  EditText changeNameView;

  @BindView(R.id.settings_chat_notification_switch)
  Switch switchChatNotification;

  @BindView(R.id.settings_created_notification_switch)
  Switch switchCreatedNotification;

  @BindView(R.id.settings_deleted_notification_switch)
  Switch switchDeletedNotification;

  @BindView(R.id.settings_refresh_button)
  Button buttonRefresh;

  @BindView(android.R.id.content)
  View view;

  FirebaseUser user;
  FirebaseFirestore db;
  MuggerUserCache muggerUserCache;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    user = FirebaseAuth.getInstance().getCurrentUser();
    db = FirebaseFirestore.getInstance();
    muggerUserCache = MuggerUserCache.getInstance();
    AlertDialog dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setMessage("Updating settings...")
        .setTheme(R.style.SpotsDialog)
        .setCancelable(false)
        .build();
    DocumentReference userRef = MuggerDatabase.getUserReference(db, user.getUid());
    super.onCreate(savedInstanceState);
    if (stopActivity) {  finish();
      return;
    }
    setContentView(R.layout.activity_settings);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ButterKnife.bind(this);
    Long createdSettings = (Long) muggerUserCache.getData().get(MessagingService.CREATED_NOTIFICATION);
    if (createdSettings == null) {
      createdSettings = 1L;
    }
    switchCreatedNotification.setChecked(!createdSettings.equals(Long.valueOf(0)));
    switchCreatedNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
      dialog.show();
      userRef.update(MessagingService.CREATED_NOTIFICATION, isChecked ? 1L : 0L)
          .addOnCompleteListener(task -> {
            muggerUserCache.getData().put(MessagingService.CREATED_NOTIFICATION, isChecked ? 1L : 0L);
            dialog.dismiss();
          });
    });
    Long deletedSettings = (Long) muggerUserCache.getData().get(MessagingService.DELETED_NOTIFICATION);
    if (deletedSettings == null) {
      deletedSettings = 1L;
    }
    switchDeletedNotification.setChecked(!deletedSettings.equals(Long.valueOf(0)));
    switchDeletedNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
      dialog.show();
      userRef.update(MessagingService.DELETED_NOTIFICATION, isChecked ? 1L : 0L)
          .addOnCompleteListener(task -> {
            muggerUserCache.getData().put(MessagingService.DELETED_NOTIFICATION, isChecked ? 1L : 0L);
            dialog.dismiss();
          });
    });
    Long chatSettings = (Long) muggerUserCache.getData().get(MessagingService.CHAT_NOTIFICATION);
    if (chatSettings == null) {
      chatSettings = 1L;
    }
    switchChatNotification.setChecked(!chatSettings.equals(Long.valueOf(0)));
    switchChatNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
      dialog.show();
      userRef.update(MessagingService.CHAT_NOTIFICATION, isChecked ? 1L : 0L)
          .addOnCompleteListener(task -> {
            muggerUserCache.getData().put(MessagingService.CHAT_NOTIFICATION, isChecked ? 1L : 0L);
            dialog.dismiss();
          });
    });
    changeNameView.setText(user.getDisplayName());
  }

  @OnClick(R.id.settings_change_display_name_button)
  public void onClick_displayName() {
    if (user == null) {
      return;
    }
    String newName = changeNameView.getText().toString();
    hideKeyboard();
    AlertDialog dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setMessage("Changing display name...")
        .setTheme(R.style.SpotsDialog)
        .setCancelable(false)
        .build();
    dialog.show();

    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newName).build()).addOnCompleteListener((task) -> {
      if (!task.isSuccessful()) {
        dialog.dismiss();
        Snackbar.make(view, "Failed to change display name, please try again later",
            Snackbar.LENGTH_SHORT).show();
      } else {
        MuggerDatabase.getUserReference(db, user.getUid()).update("displayName", newName)
            .addOnCompleteListener(task2 -> {
              dialog.dismiss();
              if (!task2.isSuccessful()) {
                Snackbar.make(view, "Failed to change display name, please try again later",
                    Snackbar.LENGTH_SHORT).show();
              } else {
                Snackbar.make(view, "Your display name has been changed successfully",
                    Snackbar.LENGTH_SHORT).show();
              }
            });
      }
    });
  }

  @OnClick(R.id.settings_refresh_button)
  public void onClick_refresh() {
    MuggerDatabase.getUserReference(db, user.getUid()).update("nusNetId", FieldValue.delete())
        .addOnCompleteListener(task -> {
          finish();
          signOut();
          Toasty.success(this, "Refreshed successfully", Toast.LENGTH_SHORT).show();
        });
  }

  private void hideKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

  private void signOut() {
    // Firebase sign out
    FirebaseAuth.getInstance().signOut();

    // Google sign out
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("292230336625-pa93l9untqrvad2mc6m3i77kckjkk4k1.apps.googleusercontent.com")
        .requestEmail()
        .build();
    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    mGoogleSignInClient.signOut();

    MuggerUserCache.clear();
  }
}
