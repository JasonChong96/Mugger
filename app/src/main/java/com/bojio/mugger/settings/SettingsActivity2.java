package com.bojio.mugger.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.fcm.MessagingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import dmax.dialog.SpotsDialog;

public class SettingsActivity2 extends AppCompatPreferenceActivity {
  private static final String TAG = SettingsActivity2.class.getSimpleName();
  private static AlertDialog dialog;
  private static Snackbar snackbar;
  private static Preference.OnPreferenceChangeListener sBindSwitchPreferenceListener = new Preference.OnPreferenceChangeListener() {
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      dialog.setMessage("Updating Settings...");
      dialog.show();
      if (user == null) {
        user = FirebaseAuth.getInstance().getCurrentUser();
      }
      if (db == null) {
        db = FirebaseFirestore.getInstance();
      }
      boolean isChecked = (Boolean) newValue;
      String type;
      switch (preference.getKey()) {
        case "toggle_delete_notifications":
          type = MessagingService.DELETED_NOTIFICATION;
          break;
        case "toggle_create_notifications":
          type = MessagingService.CREATED_NOTIFICATION;
          break;
        case "toggle_chat_notifications":
          type = MessagingService.CHAT_NOTIFICATION;
          break;
        case "toggle_unrelated_modules":
          type = "showUnrelatedModules";
          break;
        default:
          dialog.dismiss();
          return false;
      }
      MuggerUserCache.getInstance().getData().put(type, isChecked ? 1L : 0L);
      MuggerDatabase.getUserReference(db, user.getUid()).update(type, isChecked ? 1L : 0L)
          .addOnCompleteListener(task -> {
            dialog.dismiss();
          });
      return true;
    }
  };
  /**
   * A preference value change listener that updates the preference's summary
   * to reflect its new value.
   */
  private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      String stringValue = newValue.toString();
      if (user == null) {
        user = FirebaseAuth.getInstance().getCurrentUser();
      }
      if (db == null) {
        db = FirebaseFirestore.getInstance();
      }

      if (preference instanceof ListPreference) {
        // For list preferences, look up the correct display value in
        // the preference's 'entries' list.
        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(stringValue);

        // Set the summary to reflect the new value.
        preference.setSummary(
            index >= 0
                ? listPreference.getEntries()[index]
                : null);

      } else if (preference instanceof RingtonePreference) {
        // For ringtone preferences, look up the correct display value
        // using RingtoneManager.
        if (TextUtils.isEmpty(stringValue)) {
          // Empty values correspond to 'silent' (no ringtone).
          preference.setSummary(R.string.pref_ringtone_silent);

        } else {
          Ringtone ringtone = RingtoneManager.getRingtone(
              preference.getContext(), Uri.parse(stringValue));

          if (ringtone == null) {
            // Clear the summary if there was a lookup error.
            //  preference.setSummary(R.string.summary_choose_ringtone);
          } else {
            // Set the summary to reflect the new ringtone display
            // name.
            String name = ringtone.getTitle(preference.getContext());
            preference.setSummary(name);
          }
        }

      } else if (preference instanceof EditTextPreference) {
        if (preference.getKey().equals("key_gallery_name")) {
          // update the changed gallery name to summary filed
          preference.setSummary(stringValue);
        } else if (preference.getKey().equals("change_display_name")) {
          dialog.setMessage("Changing display name...");
          preference.setSummary(stringValue);
          dialog.show();
          user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(stringValue)
              .build()).addOnCompleteListener((task) -> {
            if (!task.isSuccessful()) {
              dialog.dismiss();
              snackbar.setText("Failed to change display name, please try again later");
              snackbar.show();
              preference.setSummary(user.getDisplayName());
            } else {
              MuggerDatabase.getUserReference(db, user.getUid()).update("displayName", stringValue)
                  .addOnCompleteListener(task2 -> {
                    if (!task2.isSuccessful()) {
                      snackbar.setText("Failed to change display name, please try again later")
                          .show();
                      dialog.dismiss();
                      preference.setSummary(user.getDisplayName());
                    } else {
                      Query mQuery = MuggerDatabase.getAllListingsReference(db)
                          .orderBy(user.getUid());
                      mQuery.get().addOnCompleteListener(taskk -> {
                        dialog.dismiss();
                        if (!taskk.isSuccessful()) {
                          snackbar.setText("Failed to change display name, please try again later")
                              .show();
                        } else {
                          WriteBatch batch = db.batch();
                          for (DocumentSnapshot snap : taskk.getResult().getDocuments()) {
                            batch.update(snap.getReference(), "ownerName", stringValue);
                          }

                          batch.commit().addOnCompleteListener(taskkk -> {
                            if (!taskk.isSuccessful()) {
                              snackbar.setText("Failed to change display name, please try again later")
                                  .show();
                            } else {
                              snackbar.setText("Your display name has been changed successfully").show();
                            }
                          });
                        }
                      });
                      preference.setSummary(user.getDisplayName());
                    }
                  });
            }
          });

        }
      } else {
        preference.setSummary(stringValue);
      }
      return true;
    }
  };
  private FirebaseAuth mAuth;
  private FirebaseUser user;

  private static void bindPreferenceSummaryToValue(Preference preference) {
    preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

  }

  /**
   * Email client intent to send support mail
   * Appends the necessary device information to email body
   * useful when providing support
   */
  public static void sendFeedback(Context context) {
    String body = null;
    try {
      body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
      body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
          Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
          "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
    } catch (PackageManager.NameNotFoundException e) {
    }
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("message/rfc822");
    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@androidhive.info"});
    intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
    intent.putExtra(Intent.EXTRA_TEXT, body);
    //context.startActivity(Intent.createChooser(intent, context.getString(R.string
    //    .choose_email_client)));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    mAuth = FirebaseAuth.getInstance();
    if (mAuth.getCurrentUser() == null) {
      LoggedInActivity.signOut(this);
    }
    // load settings fragment
    PreferenceFragment fragment = new MainPreferenceFragment();
    getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setCancelable(false)
        .setTheme(R.style.SpotsDialog)
        .build();
    snackbar = Snackbar.make(findViewById(android.R.id.content), "", Snackbar.LENGTH_SHORT);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  /*@Override
  public void onBackPressed() {
    Intent intent = new Intent(this, Main2Activity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
    finish();
  }*/

  public static class MainPreferenceFragment extends PreferenceFragment {
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_main);


      // gallery EditText change listener
      // bindPreferenceSummaryToValue(findPreference(getString(R.string.key_gallery_name)));

      // notification preference change listener
      //  bindPreferenceSummaryToValue(findPreference(getString(R.string
      //   .key_notifications_new_message_ringtone)));

      // feedback preference click listener
   /*   Preference myPref = findPreference(getString(R.string.key_send_feedback));
      myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
          sendFeedback(getActivity());
          return true;
        }
      });*/
      mAuth = FirebaseAuth.getInstance();
      EditTextPreference changeName = (EditTextPreference) findPreference
          (getString(R.string.settings_key_change_display_name));
      user = mAuth.getCurrentUser();
      MuggerUserCache muggerUserCache = MuggerUserCache.getInstance();
      bindPreferenceSummaryToValue(changeName);
      changeName.setText(user.getDisplayName());
      changeName.setSummary(user.getDisplayName());

      SwitchPreference deleteNotifSwitch = (SwitchPreference) findPreference(getString(R.string
          .settings_key_toggle_delete_notifications));
      deleteNotifSwitch.setOnPreferenceChangeListener(sBindSwitchPreferenceListener);
      Long deleteSettings = (Long) muggerUserCache.getData().get(MessagingService.DELETED_NOTIFICATION);
      if (deleteSettings == null) {
        deleteSettings = 1L;
      }
      deleteNotifSwitch.setChecked(!deleteSettings.equals(Long.valueOf(0)));

      SwitchPreference chatNotifSwitch = (SwitchPreference) findPreference(getString(R.string
          .settings_key_toggle_chat_notifications));
      chatNotifSwitch.setOnPreferenceChangeListener(sBindSwitchPreferenceListener);
      Long chatSettings = (Long) muggerUserCache.getData().get(MessagingService.CHAT_NOTIFICATION);
      if (chatSettings == null) {
        chatSettings = 1L;
      }
      chatNotifSwitch.setChecked(!chatSettings.equals(Long.valueOf(0)));

      SwitchPreference createNotifSwitch = (SwitchPreference) findPreference(getString(R.string
          .settings_key_toggle_create_notifications));
      createNotifSwitch.setOnPreferenceChangeListener(sBindSwitchPreferenceListener);
      Long createSettings = (Long) muggerUserCache.getData().get(MessagingService.CREATED_NOTIFICATION);
      if (createSettings == null) {
        createSettings = 1L;
      }
      chatNotifSwitch.setChecked(!createSettings.equals(Long.valueOf(0)));
      SwitchPreference unrelatedModulesSwitch = (SwitchPreference) findPreference(getString(R.string
          .settings_key_toggle_unrelated_modules));
      unrelatedModulesSwitch.setOnPreferenceChangeListener(sBindSwitchPreferenceListener);
      Long unrelatedModules = (Long) muggerUserCache.getData().get("showUnrelatedModules");
      if (unrelatedModules == null) {
        unrelatedModules = 0L;
      }
      chatNotifSwitch.setChecked(!unrelatedModules.equals(Long.valueOf(0)));
    }
  }
}